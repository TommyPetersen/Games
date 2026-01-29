(ns games.connect-four.connect-four-utilities-aiamg
  (:require [clojure.core.async :refer [go >!]])
  (:require (games [game-utilities-aiamg :as game-utils-aiamg]))
  (:require (games.connect-four [connect-four-utilities-misc :as connect-four-utils-misc]))
  (:import (java.awt Color))
  (:import (java.awt.event MouseEvent MouseAdapter))
)

(defn ny-musehaendelsesbehandler [
                                   specialiserede-grafikmodul
				   kanal			; atom
				 ]
  (fn [musehaendelse]
    (if (not= nil musehaendelse)
      (if (= (.getButton musehaendelse) MouseEvent/BUTTON1)
        (dosync
	  ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :vaelg-fokuseret-celle))
	  ((@(specialiserede-grafikmodul :funktionalitet) :rens-laerred-tegn-og-vis-alt))
	)
      )
    )
    (let [valgte-celler ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :hent-valgte-celler))]
      (if (>= (count valgte-celler) 1) (go (>! @kanal (:column-index (first valgte-celler)))))
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

(defn nyt-specialiseret-grafikmodul []
  (let [
         gaengse-grafikmodul (game-utils-aiamg/nyt-grafikmodul)
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
						         nedtaellingsramme-venstre-margin-pctr	; [50 10 85 0]
						         nedtaellingsramme-hoejre-margin-pctr	; [10 50 85 0]
						         tidsgraense
						       ]
						       ((@(gaengse-grafikmodul :funktionalitet) :fastsaet-tilstand) braet spillernummer vinduesbredde vindueshoejde vindueslokalisering-x vindueslokalisering-y braetbredde braethoejde nedtaellingsramme-venstre-margin-pctr nedtaellingsramme-hoejre-margin-pctr tidsgraense)
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
								     gyldige-traek (connect-four-utils-misc/valid-column-seq (@(gaengse-grafikmodul :tilstand) :braet) 6)
								     cellekoordinat-i-fokus-er-gyldigt? (and (not= nil cellekoordinat-i-fokus)
								                                             (let [
									                                            valg-praedikat #(= % (:column-index celleindeks-i-fokus))
										                                  ]
										                                  (boolean (some valg-praedikat gyldige-traek))
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
						                ((@(gaengse-grafikmodul :funktionalitet) :vis-laerred))
				                              )
			      }
                        )
       ]
       {:funktionalitet funktionalitet :forfaedre {:gaengse-grafikmodul gaengse-grafikmodul}}
  )
)
