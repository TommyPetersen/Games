(ns games.infection.interaktiv-grafikbaseret-spiller
  (:require [clojure.test :refer :all]
            (games [game-utilities-aiamg :as game-utils-aiamg]
                   [game-utilities-misc :as game-utils-misc])
            (games.infection [infektion-hjaelpefunktioner-aiamg :as infektion-hjlp-aiamg]
                             [infektion-hjaelpefunktioner-diverse :as infektion-hjlp-div])
            [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.core.async :refer [go >! <!! timeout]]
  )
  (:import (java.awt.event MouseAdapter))
)


    ;;;;;;;;;;;;;;;;;;;;;;
    ;;;                ;;;
    ;;; * Infektion  * ;;;
    ;;;                ;;;
    ;;;;;;;;;;;;;;;;;;;;;;


(defn ny-spiller [spillernummer]
  (let [
         specialiserede-grafikmodul infektion-hjlp-aiamg/specialiserede-grafikmodul
         vinduesbredde 800
         vindueshoejde 600
         base-frame (game-utils-aiamg/calculate-base-frame vinduesbredde vindueshoejde)
         cell-grid-coords (game-utils-aiamg/generate-cell-grid-coords 7 7 base-frame)
         border-coords (:border-coords cell-grid-coords)
         cell-coords (:cell-coords cell-grid-coords)
         braet (atom (infektion-hjlp-div/init-board "*" "¤"))
         braethistorik (atom [@braet])
         tidsenhed 1000
         tidsgraense 120000
         historiklaengde 15
         spillerbrik (if (= spillernummer 1) "*" "¤")
         modstanderbrik (if (= spillernummer 1) "¤" "*")
         fortsaet-nedtaelling-for-modstander (atom (atom false))
         behandl-alle-musehaendelsestyper (atom false)
         kanal-til-hent-spillertraek (atom nil)
         tegn-nedtaellinger (fn [
                                  samlet-tid-for-spiller
                                  samlet-tid-for-modstander
                                  tidsgraense
                                ]
                              (try
                                (dosync
                                  ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :samlet-tid-for-spiller samlet-tid-for-spiller)
                                  ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :samlet-tid-for-modstander samlet-tid-for-modstander)
                                  ((@(specialiserede-grafikmodul :funktionalitet) :rens-laerred-tegn-og-vis-alt))
                                )
                                (catch NullPointerException npe
                                  (go (>! @kanal-til-hent-spillertraek {:fra-koordinat [-1 -1] :til-koordinat [-1 -1]}))
                                  (throw npe)
                                )
                              )
                            )
         hent-spillertraek (fn [spillernummer]
                             (if (infektion-hjlp-div/cannot-move? @braet spillerbrik)
                               (str {:fra-koordinat [-1 -1] :til-koordinat [-1 -1]})
                               (let [
                                      go-loop-result-player (game-utils-misc/go-loop-on-atom
                                                              (fn [v]
                                                                (tegn-nedtaellinger v 0 tidsgraense)
                                                              )
                                                              tidsenhed tidsgraense
                                                            )
                                      fortsaet-nedtaelling-for-spiller (:continue-going go-loop-result-player)
                                      _ (reset! behandl-alle-musehaendelsestyper true)
                                      _ (reset! kanal-til-hent-spillertraek (timeout tidsgraense))
                                      move (let [hentet-spillertraek (<!! @kanal-til-hent-spillertraek)]
                                             (if (= hentet-spillertraek nil)
                                               {:from-cell {:row-index -1 :column-index -1} :to-cell {:row-index -1 :column-index -1}}
                                               hentet-spillertraek
                                             )
                                           )
                                      _ (reset! behandl-alle-musehaendelsestyper false)
                                      _ ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :valgte-celler-indekseret [])
                                      _ ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :fokuseret-celle-indekseret nil)
                                      _ ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :fokuseret-celle-rammefarve nil)
                                      _ (reset! fortsaet-nedtaelling-for-spiller false)
                                    ]
                                    (if (infektion-hjlp-div/move-valid? @braet spillerbrik move)
                                      (do
                                        (swap! braet infektion-hjlp-div/make-move move)
                                        (swap! braethistorik conj @braet)
                                        (dosync
                                            ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :braet @braet)
                                            ((@(specialiserede-grafikmodul :funktionalitet) :opdater-tilstand) :braethistorik @braethistorik)
                                        )
                                        (if (infektion-hjlp-div/can-move? @braet modstanderbrik)
                                          (let [
                                                 go-loop-result-opponent (game-utils-misc/go-loop-on-atom
                                                                           (fn [v]
                                                                             (tegn-nedtaellinger 0 v tidsgraense)
                                                                           )
                                                                           tidsenhed tidsgraense
                                                                         )
                                               ]
                                               (reset! fortsaet-nedtaelling-for-modstander (:continue-going go-loop-result-opponent))
                                          )
                                          (tegn-nedtaellinger 0 0 tidsgraense)
                                        )
                                      )
                                    )
                                    (str move)
                               )
                             )
                         )
         update-board (fn [unit-input]
                          (let [
                                 move-string (nth (:data unit-input) 1)
                                 move (edn/read-string move-string)
                                 from-cell {:row-index (second (:fra-koordinat move)) :column-index (first (:fra-koordinat move))}
                                 to-cell {:row-index (second (:til-koordinat move)) :column-index (first (:til-koordinat move))}
                               ]
                               (reset! @fortsaet-nedtaelling-for-modstander false)
                               (if (infektion-hjlp-div/move-valid? @braet modstanderbrik move)
                                 (do
                                   (swap! braet infektion-hjlp-div/make-move move)
                                   (swap! braethistorik conj @braet)
                                   (dosync
                                     ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :braet @braet)
                                     ((@(specialiserede-grafikmodul :funktionalitet) :opdater-tilstand) :braethistorik @braethistorik)
                                   )
                                 )
                               )
                               (dosync
                                 ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :valgte-celler-indekseret [{:row-index (second (:fra-koordinat move)) :column-index (first (:fra-koordinat move))} {:row-index (second (:til-koordinat move)) :column-index (first (:til-koordinat move))}])
                                 ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :samlet-tid-for-spiller 0)
                                 ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :samlet-tid-for-modstander 0)
                                 ((@(specialiserede-grafikmodul :funktionalitet) :rens-laerred-tegn-og-vis-alt))
                                 ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :valgte-celler-indekseret [])
                               )
                          )
                      )
       ]
       (fn [enheds-inddata]
         (let [first-data-element (first (:data enheds-inddata))]
             (case first-data-element
                "initialiserSpil"         (let [
                                                 dialogsystem (edn/read-string (second (:data enheds-inddata)))
                                               ]
                                               (reset! braet (infektion-hjlp-div/init-board "*" "¤"))
                                               (reset! braethistorik [@braet])
                                               (dosync
                                                 ((@(specialiserede-grafikmodul :funktionalitet) :fastsaet-tilstand) @braet spillernummer vinduesbredde vindueshoejde (dialogsystem :vindueslokalisering-x) (dialogsystem :vindueslokalisering-y) 7 7 [50 10 85 0] [10 50 85 0] 120000 historiklaengde @braethistorik)
                                                 ((@(specialiserede-grafikmodul :funktionalitet) :rens-laerred-tegn-og-vis-alt))
                                               )
                                               (reset! kanal-til-hent-spillertraek (timeout tidsgraense))
                                               (let [
                                                      fn-behandl-musehaendelse (infektion-hjlp-aiamg/ny-musehaendelsesbehandler specialiserede-grafikmodul kanal-til-hent-spillertraek)
                                                      fn-behandl-musebevaegelseshaendelse (infektion-hjlp-aiamg/ny-musebevaegelsehaendelsesbehandler specialiserede-grafikmodul)
                                                      skaerm (.getScreen (@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :tilstand) :kamera))
                                                    ]
                                                 (.addMouseListener skaerm (proxy [MouseAdapter] []
                                                                             (mouseClicked [musehaendelse]
                                                                               (if @behandl-alle-musehaendelsestyper
                                                                                 (fn-behandl-musehaendelse musehaendelse)
                                                                               )
                                                                             )
                                                                           )
                                                 )
                                                 (.addMouseMotionListener skaerm (proxy [MouseAdapter] []
                                                                                   (mouseMoved [musebevaegelseshaendelse]
                                                                                     (if @behandl-alle-musehaendelsestyper
                                                                                       (fn-behandl-musebevaegelseshaendelse musebevaegelseshaendelse)
                                                                                     )
                                                                                   )
                                                                                 )
                                                 )
                                               )
                                               {:data ["Ok"]}
                                          )
                "hentFoersteTraek"        {:data [(str (hent-spillertraek spillernummer))]}
                "hentNaesteTraek"         (do
                                            (update-board enheds-inddata)
                                            {:data [(str (hent-spillertraek spillernummer))]}
                                          )
                "meddelTraek"             (do
                                            (update-board enheds-inddata)
                                            {:data ["Accepteret"]}
                                          )

                {:data ["Fejl i data"]}
             )
        )
      )
  )
)


(def spiller1 (ny-spiller 1))
(def spiller2 (ny-spiller 2))
