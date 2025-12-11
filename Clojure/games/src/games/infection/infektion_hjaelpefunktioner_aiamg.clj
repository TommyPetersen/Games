(ns games.infection.infektion-hjaelpefunktioner-aiamg
  (:require [clojure.core.async :refer [go >!]])
  (:require (games [game-utilities-aiamg :as game-utils-aiamg])
            (games.infection [infektion-hjaelpefunktioner-diverse :as infektion-hjlp-div]))
  (:import (java.awt Color))
  (:import (java.awt.event MouseEvent))
  (:import (java.awt Color)(Aiamg Camera Polygon3D Point3D Line3D))
)

(defn tegn-grafer [
                    kamera                      ; Aiamg.Camera
                    datapoints                  ; [{keyword("*", "¤") count}]
                    max-no-datapoints-shown     ; Integer
                    graph-frame-x0              ; Graf frame's origo-x
                    graph-frame-y0              ; Graf frame's origo-y
                    graph-frame-x1              ; Graf frame's right limit
                    graph-frame-y1              ; Graf frame's top limit
                  ]
  (let [
         y-value-fn #(+ graph-frame-y0 (* % (- graph-frame-y1 graph-frame-y0)))
         color1 Color/white
         color2 Color/red
         color-avail Color/darkGray
         prev-point3d-pl1 (atom (new Point3D graph-frame-x0 (y-value-fn (/ ((keyword "*") (first datapoints)) 49)) game-utils-aiamg/projection-plane-z color1))
         prev-point3d-pl2 (atom (new Point3D graph-frame-x0 (y-value-fn (/ ((keyword "¤") (first datapoints)) 49)) game-utils-aiamg/projection-plane-z color2))
         prev-point3d-avail (atom (new Point3D graph-frame-x0 (y-value-fn (/ (- 49 (+ ((keyword "*") (first datapoints)) ((keyword "¤") (first datapoints)))) 49)) game-utils-aiamg/projection-plane-z color-avail))
         delta-x (/ (- graph-frame-x1 graph-frame-x0) (- max-no-datapoints-shown 1))
       ]
       (doseq [
                datapoint (vec (rest datapoints))
              ]
              (let [
                     curr-point3d-pl1 (new Point3D (+ (.x @prev-point3d-pl1) delta-x) (y-value-fn (/ ((keyword "*") datapoint) 49)) game-utils-aiamg/projection-plane-z color1)
                     line3d-pl1 (new Line3D @prev-point3d-pl1 curr-point3d-pl1)
                     curr-point3d-pl2 (new Point3D (+ (.x @prev-point3d-pl2) delta-x) (y-value-fn (/ ((keyword "¤") datapoint) 49)) game-utils-aiamg/projection-plane-z color2)
                     line3d-pl2 (new Line3D @prev-point3d-pl2 curr-point3d-pl2)
                     curr-point3d-avail (new Point3D (+ (.x @prev-point3d-pl2) delta-x) (y-value-fn (/ (- 49 (+ ((keyword "*") datapoint) ((keyword "¤") datapoint))) 49)) game-utils-aiamg/projection-plane-z color-avail)
                     line3d-avail (new Line3D @prev-point3d-avail curr-point3d-avail)
                   ]
                   (doto kamera
                     (.updateScene line3d-pl1)
                     (.updateScene line3d-pl2)
                     (.updateScene line3d-avail)
                   )
                   (reset! prev-point3d-pl1 curr-point3d-pl1)
                   (reset! prev-point3d-pl2 curr-point3d-pl2)
                   (reset! prev-point3d-avail curr-point3d-avail)
              )
       )
  )
)

(defn ny-musehaendelsesbehandler [
                                   specialiserede-grafikmodul
                                   kanal                        ; atom
                                 ]
  (fn [musehaendelse]
    (if (not= nil musehaendelse)
      (if (= (.getButton musehaendelse) MouseEvent/BUTTON1)
        (do
          (dosync
            ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :vaelg-fokuseret-celle))
            ((@(specialiserede-grafikmodul :funktionalitet) :rens-laerred-tegn-og-vis-alt))
          )
          (let [valgte-celler ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :hent-valgte-celler))]
            (if (>= (count valgte-celler) 2)
              (go (>! @kanal {:fra-koordinat [(:column-index (first valgte-celler)) (:row-index (first valgte-celler))]
                              :til-koordinat [(:column-index (second valgte-celler)) (:row-index (second valgte-celler))]}))
            )
          )
        )
        (if (= (.getButton musehaendelse) MouseEvent/BUTTON3)
          (dosync
            ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :fravaelg-alle-valgte-celler))
            ((@(specialiserede-grafikmodul :funktionalitet) :rens-laerred-tegn-og-vis-alt))
          )
        )
      )
    )
  )
)

(defn ny-musebevaegelsehaendelsesbehandler [specialiserede-grafikmodul]
  (fn [musebevaegelseshaendelse]
    (dosync
      ((@(specialiserede-grafikmodul :funktionalitet) :fokuser-paa-celle) musebevaegelseshaendelse)
      ((@(specialiserede-grafikmodul :funktionalitet) :rens-laerred-tegn-og-vis-alt))
    )
  )
)

;;; Specialiseret grafikmodul ;;;

(def specialiserede-grafikmodul
  (let [
         gaengse-grafikmodul game-utils-aiamg/grafikmodul
         tilstand (ref {
                         :historiklaengde nil
                         :braethistorik nil
                         :statistikramme nil
                       }
                  )
         funktionalitet (atom {
                                :fastsaet-tilstand (fn [
                                                         ; Gaengse parametre ;
                                                         braet
                                                         spillernummer
                                                         vinduesbredde
                                                         vindueshoejde
                                                         vindueslokalisering-x
                                                         vindueslokalisering-y
                                                         braetbredde
                                                         braethoejde
                                                         nedtaellingsramme-venstre-margin-pctr  ; [50 10 85 0]
                                                         nedtaellingsramme-hoejre-margin-pctr   ; [10 50 85 0]
                                                         tidsgraense
                                                         ; Specialiserede parametre ;
                                                         historiklaengde
                                                         braethistorik
                                                       ]
                                                       ((@(gaengse-grafikmodul :funktionalitet) :fastsaet-tilstand) braet spillernummer vinduesbredde vindueshoejde vindueslokalisering-x vindueslokalisering-y braetbredde braethoejde nedtaellingsramme-venstre-margin-pctr nedtaellingsramme-hoejre-margin-pctr tidsgraense)
                                                       (let [
                                                              graensekoordinater ((@(gaengse-grafikmodul :tilstand) :cell-grid-coords) :border-coords)
                                                              spilramme (@(gaengse-grafikmodul :tilstand) :base-frame)
                                                              statistikramme (game-utils-aiamg/calculate-aux-frame (:left graensekoordinater) (+ (:left graensekoordinater) (* 20 historiklaengde)) (:base-frame-top-border spilramme) (:top graensekoordinater) [0 0 10 10])
                                                            ]
                                                            (dosync
                                                              (alter tilstand assoc :historiklaengde historiklaengde
                                                                                    :braethistorik braethistorik
                                                                                    :statistikramme statistikramme
                                                              )
                                                            )
                                                       )
                                                   )
                                :opdater-tilstand (fn [
                                                        noegle
                                                        vaerdi
                                                      ]
                                                    (dosync (alter tilstand assoc noegle vaerdi))
                                                  )
                                :fokuser-paa-celle (fn [
                                                         musebevaegelseshaendelse
                                                       ]
                                                     (if (not= nil musebevaegelseshaendelse)
                                                       (let [
                                                              kamera (@(gaengse-grafikmodul :tilstand) :kamera)
                                                              indsats (.getInsetsOnScreen kamera)
                                                              projektionsplanskoordinater (game-utils-aiamg/transform-coords-in-mouse-event kamera indsats musebevaegelseshaendelse)
                                                              projektionsplan-x (:transformed-x projektionsplanskoordinater)
                                                              projektionsplan-y (:transformed-y projektionsplanskoordinater)
                                                              graensekoordinater ((@(gaengse-grafikmodul :tilstand) :cell-grid-coords) :border-coords)
                                                            ]
                                                            (if (and (not= nil projektionsplan-x) (>= projektionsplan-x (double (:left graensekoordinater))) (<= projektionsplan-x (double (:right graensekoordinater)))
                                                                     (not= nil projektionsplan-y) (>= projektionsplan-y (double (:bottom graensekoordinater))) (<= projektionsplan-y (double (:top graensekoordinater)))
                                                                )
                                                              (let [
                                                                     cellekoordinater ((@(gaengse-grafikmodul :tilstand) :cell-grid-coords) :cell-coords)
                                                                     cellekoordinat-i-fokus (game-utils-aiamg/find-cell-coord projektionsplan-x projektionsplan-y cellekoordinater)
                                                                     celleindeks-i-fokus {:row-index (:row-index cellekoordinat-i-fokus) :column-index (:column-index cellekoordinat-i-fokus)}
                                                                     gyldige-traek (infektion-hjlp-div/valid-move-seq (@(gaengse-grafikmodul :tilstand) :braet) (@(gaengse-grafikmodul :tilstand) :spillerbrik))
                                                                     forrige-valgte-celleindeks (first (@(gaengse-grafikmodul :tilstand) :valgte-celler-indekseret))
                                                                     cellekoordinat-i-fokus-er-gyldigt?
                                                                       (and (not= nil cellekoordinat-i-fokus)
                                                                            (if (= nil forrige-valgte-celleindeks)
                                                                              (let [
                                                                                     foerste-valg-praedikat #(and (= (first (:fra-koordinat %)) (:column-index celleindeks-i-fokus))
                                                                                                                  (= (second (:fra-koordinat %)) (:row-index celleindeks-i-fokus)))
                                                                                   ]
                                                                                   (boolean (some foerste-valg-praedikat gyldige-traek))
                                                                              )
                                                                              (let [
                                                                                     andet-valg-praedikat #(and (= (first (:fra-koordinat %)) (:column-index forrige-valgte-celleindeks))
                                                                                                                (= (second (:fra-koordinat %)) (:row-index forrige-valgte-celleindeks))
                                                                                                                (= (first (:til-koordinat %)) (:column-index celleindeks-i-fokus))
                                                                                                                (= (second (:til-koordinat %)) (:row-index celleindeks-i-fokus)))
                                                                                   ]
                                                                                   (boolean (some andet-valg-praedikat gyldige-traek))
                                                                              )
                                                                            )
                                                                       )
                                                                   ]
                                                                   (if cellekoordinat-i-fokus-er-gyldigt?
                                                                     (do
                                                                       ((@(gaengse-grafikmodul :funktionalitet) :opdater-tilstand) :fokuseret-celle-indekseret celleindeks-i-fokus)
                                                                       ((@(gaengse-grafikmodul :funktionalitet) :opdater-tilstand) :fokuseret-celle-rammefarve {:top Color/gray :bottom Color/gray :left Color/gray :right Color/gray})
                                                                     )
                                                                     (do
                                                                       ((@(gaengse-grafikmodul :funktionalitet) :opdater-tilstand) :fokuseret-celle-indekseret nil)
                                                                       ((@(gaengse-grafikmodul :funktionalitet) :opdater-tilstand) :fokuseret-celle-rammefarve nil)
                                                                     )
                                                                   )
                                                              )
                                                              (do
                                                                ((@(gaengse-grafikmodul :funktionalitet) :opdater-tilstand) :fokuseret-celle-indekseret nil)
                                                                ((@(gaengse-grafikmodul :funktionalitet) :opdater-tilstand) :fokuseret-celle-rammefarve nil)
                                                              )
                                                            )
                                                       )
                                                     )
                                                   )
                                :rens-laerred-tegn-og-vis-alt (fn []
                                                                    ((@(gaengse-grafikmodul :funktionalitet) :rens-laerred))
                                                                    ((@(gaengse-grafikmodul :funktionalitet) :tegn-de-gaengse-figurer))
                                                                    (let [
                                                                           kamera (@(gaengse-grafikmodul :tilstand) :kamera)
                                                                           spillerbrik (@(gaengse-grafikmodul :tilstand) :spillerbrik)
                                                                           modstanderbrik (@(gaengse-grafikmodul :tilstand) :modstanderbrik)
                                                                           statistikramme (@tilstand :statistikramme)
                                                                           braethistorik (@tilstand :braethistorik)
                                                                           historiklaengde (@tilstand :historiklaengde)
                                                                           seneste-braethistorik (drop (- (count braethistorik) historiklaengde) braethistorik)
                                                                         ]
                                                                         (game-utils-aiamg/show-aux-frame kamera statistikramme)
                                                                         (tegn-grafer kamera (infektion-hjlp-div/count-symbols-in-boards seneste-braethistorik spillerbrik modstanderbrik) historiklaengde (+ 5 (:frame-x0 statistikramme)) (+ 1 (:frame-y0 statistikramme)) (- (:frame-x1 statistikramme) 5) (- (:frame-y1 statistikramme) 5))
                                                                    )
                                                                    ((@(gaengse-grafikmodul :funktionalitet) :vis-laerred))
                                                              )
                              }
                        )
       ]
       {:tilstand tilstand :funktionalitet funktionalitet :forfaedre {:gaengse-grafikmodul gaengse-grafikmodul}}
  )
)
