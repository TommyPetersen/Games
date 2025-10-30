(ns games.connect-four.connect-four-utilities-aiamg
  (:require (games [game-utilities-aiamg :as game-utils-aiamg]))
  (:require (games.connect-four [connect-four-utilities-misc :as connect-four-utils-misc]))
  (:import (java.awt Color))
  (:import (java.awt.event MouseEvent))
)

(defn get-user-move [
		      specialiserede-grafikmodul	; Specialiseret grafikmodul
		      interrupt
		    ]
  (let [
	 kamera (@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :tilstand) :kamera)
	 behandl-musehaendelse (fn [
	                             musehaendelse
				   ]
				 (if (not= nil musehaendelse)
			           (if (= (.getButton musehaendelse) MouseEvent/BUTTON1)
				     ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :kald-funktion-med-laas)
				       (fn []
				         ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :vaelg-fokuseret-celle))
				         ((@(specialiserede-grafikmodul :funktionalitet) :rens-laerred-tegn-og-vis-alt))
				       )
				       []
				     )
				   )
				 )
				 ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :hent-valgte-celler))
			       )
	 behandl-musebevaegelseshaendelse (fn [
	                                        musebevaegelseshaendelse
				              ]
					    ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :kald-funktion-med-laas)
					      (fn []
				                ((@(specialiserede-grafikmodul :funktionalitet) :fokuser-paa-celle) musebevaegelseshaendelse)
				                ((@(specialiserede-grafikmodul :funktionalitet) :rens-laerred-tegn-og-vis-alt))
					      )
					      []
				            )
					  )
	 find-celler-fn (fn []
	                  (loop [
			          valgte-celler (behandl-musehaendelse (.getCurrentMouseEventOnScreen kamera))
			        ]
			        (Thread/sleep 250)
			        (if @interrupt
			          -1
				  (if (>= (count valgte-celler) 1)
				    (:column-index (first valgte-celler))
				    (do
				      (behandl-musebevaegelseshaendelse (.getCurrentMouseMovedEventOnScreen kamera))
				      (recur (behandl-musehaendelse (.getCurrentMouseEventOnScreen kamera)))
				    )
			          )
			        )
			   )
	                 )
       ]
       (find-celler-fn)
 )
)

;;; Specialiseret grafikmodul ;;;

(def specialiserede-grafikmodul
  (let [
         gaengse-grafikmodul game-utils-aiamg/grafikmodul
         funktionalitet (atom {
	                        :fastsaet-tilstand (fn [
				                         ; Gaengse parametre ;
				                         braet
				                         spillernummer
						         vinduesbredde
				                         vindueshoejde
						         braetbredde
						         braethoejde
						         nedtaellingsramme-venstre-margin-pctr	; [50 10 85 0]
						         nedtaellingsramme-hoejre-margin-pctr	; [10 50 85 0]
						         tidsgraense
						       ]
						       ((@(gaengse-grafikmodul :funktionalitet) :fastsaet-tilstand) braet spillernummer vinduesbredde vindueshoejde braetbredde braethoejde nedtaellingsramme-venstre-margin-pctr nedtaellingsramme-hoejre-margin-pctr tidsgraense)
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
							               (swap! (gaengse-grafikmodul :tilstand) assoc :fokuseret-celle-indekseret celleindeks-i-fokus)
							               (swap! (gaengse-grafikmodul :tilstand) assoc :fokuseret-celle-rammefarve {:top Color/gray :bottom Color/gray :left Color/gray :right Color/gray})
							             )
								     (do
							               (swap! (gaengse-grafikmodul :tilstand) assoc :fokuseret-celle-indekseret nil)
							               (swap! (gaengse-grafikmodul :tilstand) assoc :fokuseret-celle-rammefarve nil)
							             )
								   )
							      )
							      (do
							        (swap! (gaengse-grafikmodul :tilstand) assoc :fokuseret-celle-indekseret nil)
							        (swap! (gaengse-grafikmodul :tilstand) assoc :fokuseret-celle-rammefarve nil)
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
