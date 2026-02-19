(ns games.connect-four.interaktiv-grafikbaseret-spiller
  (:require [clojure.test :refer :all]
            (games [game-utilities-aiamg :as game-utils-aiamg]
                   [game-utilities-misc :as game-utils-misc])
            (games.connect-four [connect-four-utilities-aiamg :as connect-four-utils-aiamg]
                                [connect-four-utilities-misc :as connect-four-utils-misc])
            [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.core.async :refer [go >! <!! timeout]]
  )
  (:import (java.awt.event MouseAdapter ComponentAdapter))
)

    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;                      ;;;
    ;;; * Fire paa stribe  * ;;;
    ;;;                      ;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn ny-spiller [spillernummer]
  (let [
         specialiserede-grafikmodul (atom (connect-four-utils-aiamg/nyt-specialiseret-grafikmodul))
         vinduesbredde 800
         vindueshoejde 600
         base-frame (game-utils-aiamg/calculate-base-frame vinduesbredde vindueshoejde)
         cell-grid-coords (game-utils-aiamg/generate-cell-grid-coords 7 6 base-frame)
         border-coords (:border-coords cell-grid-coords)
         cell-coords (:cell-coords cell-grid-coords)
         braet (atom (connect-four-utils-misc/empty-board 7))
         tidsenhed 1000
         tidsgraense 120000
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
                                  ((@(((@specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :samlet-tid-for-spiller samlet-tid-for-spiller)
                                  ((@(((@specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :samlet-tid-for-modstander samlet-tid-for-modstander)
                                  ((@(@specialiserede-grafikmodul :funktionalitet) :rens-laerred-tegn-og-vis-alt))
                                )
                                (catch NullPointerException npe
                                  (go (>! @kanal-til-hent-spillertraek {:fra-koordinat [-1 -1] :til-koordinat [-1 -1]}))
                                  (throw npe)
                                )
                              )
                            )
         hent-spillertraek (fn [spillernummer]
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
                                    j (let [i (<!! @kanal-til-hent-spillertraek)] (if (= i nil) -1 i))
                                    _ (reset! behandl-alle-musehaendelsestyper false)
                                    _ ((@(((@specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :valgte-celler-indekseret [])
                                    _ ((@(((@specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :fokuseret-celle-indekseret nil)
                                    _ ((@(((@specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :fokuseret-celle-rammefarve nil)
                                    _ (reset! fortsaet-nedtaelling-for-spiller false)
                                  ]
                                  (if (connect-four-utils-misc/column-valid? @braet 7 6 j)
                                    (do
                                      (swap! braet connect-four-utils-misc/insert j spillerbrik)
                                      (dosync
                                        ((@(((@specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :braet @braet)
                                      )
                                      (if (connect-four-utils-misc/is-not-full? @braet 6)
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
                                  j
                             )
                         )
         update-board (fn [enheds-inddata]
                        (let [
                               move-string (nth (:data enheds-inddata) 1)
                               j (Integer/parseInt move-string)
                             ]
                             (reset! @fortsaet-nedtaelling-for-modstander false)
                             (if (connect-four-utils-misc/column-valid? @braet 7 6 j)
                               (do
                                 (swap! braet connect-four-utils-misc/insert j modstanderbrik)
                                 (dosync
                                   ((@(((@specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :braet @braet)
                                 )
                               )
                             )
                             (dosync
                               ((@(((@specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :valgte-celler-indekseret [{:row-index 5 :column-index j}])
                               ((@(((@specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :samlet-tid-for-spiller 0)
                               ((@(((@specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :samlet-tid-for-modstander 0)
                               ((@(@specialiserede-grafikmodul :funktionalitet) :rens-laerred-tegn-og-vis-alt))
                               ((@(((@specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :valgte-celler-indekseret [])
                             )
                        )
                      )
       ]
       (fn [enheds-inddata]
         (let [first-data-element (first (:data enheds-inddata))]
             (case first-data-element
                "initialiserSpil"         (let [
                                                 medsendte-dialogsystem (edn/read-string (second (:data enheds-inddata)))
                                                 dialogsystem (if (= nil medsendte-dialogsystem) {:vindueslokalisering-x 200 :vindueslokalisering-y 200} medsendte-dialogsystem)
                                               ]
                                               (reset! specialiserede-grafikmodul (connect-four-utils-aiamg/nyt-specialiseret-grafikmodul))
                                               (reset! braet (connect-four-utils-misc/empty-board 7))
                                               (dosync
                                                 ((@(@specialiserede-grafikmodul :funktionalitet) :fastsaet-tilstand) @braet spillernummer vinduesbredde vindueshoejde (dialogsystem :vindueslokalisering-x) (dialogsystem :vindueslokalisering-y) 7 6 [50 10 85 0] [10 50 85 0] 120000)
                                                 ((@(@specialiserede-grafikmodul :funktionalitet) :rens-laerred-tegn-og-vis-alt))
                                               )
                                               (reset! kanal-til-hent-spillertraek (timeout tidsgraense))
                                               (let [
                                                      fn-behandl-musehaendelse (connect-four-utils-aiamg/ny-musehaendelsesbehandler @specialiserede-grafikmodul kanal-til-hent-spillertraek)
                                                      fn-behandl-musebevaegelseshaendelse (connect-four-utils-aiamg/ny-musebevaegelsehaendelsesbehandler @specialiserede-grafikmodul)
                                                      skaerm (.getScreen (@(((@specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :tilstand) :kamera))
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
                                                    (.addComponentListener skaerm (proxy [ComponentAdapter] []
                                                                                    (componentResized [komponenthaendelse]
                                                                                      (let [
                                                                                             vin-bredde (.getWidth (.getScreen (@(((@specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :tilstand) :kamera)))
                                                                                             vin-hoejde (.getHeight (.getScreen (@(((@specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :tilstand) :kamera)))
                                                                                           ]
                                                                                           (dosync
                                                                                             ((@(((@specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-vinduesstoerrelse) vin-bredde vin-hoejde 7 6 [50 10 85 0] [10 50 85 0])
                                                                                             ((@(@specialiserede-grafikmodul :funktionalitet) :rens-laerred-tegn-og-vis-alt))
                                                                                           )
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

(def aktuel-spiller1 (atom nil))
(def aktuel-spiller2 (atom nil))

(defn spiller1 [enheds-inddata]
  (let [
         foerste-dataelement (first (:data enheds-inddata))
       ]
       (if (= foerste-dataelement "initialiserSpil") (reset! aktuel-spiller1 (ny-spiller 1)))
       (@aktuel-spiller1 enheds-inddata)
  )
)

(defn spiller2 [enheds-inddata]
  (let [
         foerste-dataelement (first (:data enheds-inddata))
       ]
       (if (= foerste-dataelement "initialiserSpil") (reset! aktuel-spiller2 (ny-spiller 2)))
       (@aktuel-spiller2 enheds-inddata)
  )
)
