(ns games.infection.infection-utilities-aiamg
  (:require (games [game-utilities-aiamg :as game-utils-aiamg])
            (games.infection [infection-utilities-misc :as infection-utils-misc]))
  (:import (java.awt Color))
  (:import (java.awt.event MouseEvent))
  (:import (java.awt Color)(Aiamg Camera Polygon3D Point3D Line3D))
)

(defn tegn-grafer [
                    kamera			; Aiamg.Camera
                    datapoints			; [{keyword("*", "¤") count}]
		    max-no-datapoints-shown	; Integer
		    graph-frame-x0		; Graf frame's origo-x
		    graph-frame-y0		; Graf frame's origo-y
    		    graph-frame-x1		; Graf frame's right limit
		    graph-frame-y1		; Graf frame's top limit
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

(defn get-user-selection ; {:from-cell from-cell :to-cell to-cell}
            		 [
			   specialiserede-grafikmodul	; Specialiseret grafikmodul
			   interrupt			; Atom til at afgoere om brugervalget skal afbrydes
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
				     (if (= (.getButton musehaendelse) MouseEvent/BUTTON3)
				       ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :kald-funktion-med-laas)
				         (fn []
				           ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :fravaelg-alle-valgte-celler))
				           ((@(specialiserede-grafikmodul :funktionalitet) :rens-laerred-tegn-og-vis-alt))
					 )
					 []
				       )
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
			          {:from-cell {:row-index -1 :column-index -1} :to-cell {:row-index -1 :column-index -1}}
				  (if (>= (count valgte-celler) 2)
				    {:from-cell (first valgte-celler) :to-cell (last valgte-celler)}
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

(defn get-user-move [
		      specialiserede-grafikmodul	; Specialiseret grafikmodul
		      interrupt
		    ]
  (let [
         selected-move (get-user-selection specialiserede-grafikmodul interrupt)
       ]
       {
         :from-coord [(:column-index (:from-cell selected-move)) (:row-index (:from-cell selected-move))]
         :to-coord [(:column-index (:to-cell selected-move)) (:row-index (:to-cell selected-move))]
       }
  )
)

;;; Specialiseret grafikmodul ;;;

(def specialiserede-grafikmodul
  (let [
         gaengse-grafikmodul game-utils-aiamg/grafikmodul
         tilstand (atom {
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
						         braetbredde
						         braethoejde
						         nedtaellingsramme-venstre-margin-pctr	; [50 10 85 0]
						         nedtaellingsramme-hoejre-margin-pctr	; [10 50 85 0]
						         tidsgraense
							 ; Specialiserede parametre ;
							 historiklaengde
							 braethistorik
						       ]
						       ((@(gaengse-grafikmodul :funktionalitet) :fastsaet-tilstand) braet spillernummer vinduesbredde vindueshoejde braetbredde braethoejde nedtaellingsramme-venstre-margin-pctr nedtaellingsramme-hoejre-margin-pctr tidsgraense)
						       (let [
						              graensekoordinater ((@(gaengse-grafikmodul :tilstand) :cell-grid-coords) :border-coords)
							      spilramme (@(gaengse-grafikmodul :tilstand) :base-frame)
						              statistikramme (game-utils-aiamg/calculate-aux-frame (:left graensekoordinater) (+ (:left graensekoordinater) (* 20 historiklaengde)) (:base-frame-top-border spilramme) (:top graensekoordinater) [0 0 10 10])
							    ]
						            (reset! tilstand {
							                       :historiklaengde historiklaengde
								               :braethistorik braethistorik
									       :statistikramme statistikramme
									     }
							    )
						       )
						   )
				:opdater-tilstand (fn [
				                        noegle
							vaerdi
						      ]
						    (swap! tilstand assoc noegle vaerdi)
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
								     gyldige-traek (infection-utils-misc/valid-move-seq (@(gaengse-grafikmodul :tilstand) :braet) (@(gaengse-grafikmodul :tilstand) :spillerbrik))
								     forrige-valgte-celleindeks (first (@(gaengse-grafikmodul :tilstand) :valgte-celler-indekseret))
								     cellekoordinat-i-fokus-er-gyldigt?
								       (and (not= nil cellekoordinat-i-fokus)
					                                    (if (= nil forrige-valgte-celleindeks)
									      (let [
									             foerste-valg-praedikat #(and (= (first (:from-coord %)) (:column-index celleindeks-i-fokus))
										                                  (= (second (:from-coord %)) (:row-index celleindeks-i-fokus)))
										   ]
										   (boolean (some foerste-valg-praedikat gyldige-traek))
									      )
									      (let [
									             andet-valg-praedikat #(and (= (first (:from-coord %)) (:column-index forrige-valgte-celleindeks))
										                                (= (second (:from-coord %)) (:row-index forrige-valgte-celleindeks))
									                                        (= (first (:to-coord %)) (:column-index celleindeks-i-fokus))
										                                (= (second (:to-coord %)) (:row-index celleindeks-i-fokus)))
										   ]
										   (boolean (some andet-valg-praedikat gyldige-traek))
									      )
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
						                     (tegn-grafer kamera (infection-utils-misc/count-symbols-in-boards seneste-braethistorik spillerbrik modstanderbrik) historiklaengde (+ 5 (:frame-x0 statistikramme)) (+ 1 (:frame-y0 statistikramme)) (- (:frame-x1 statistikramme) 5) (- (:frame-y1 statistikramme) 5))
	                                                        )
						                ((@(gaengse-grafikmodul :funktionalitet) :vis-laerred))
				                              )
			      }
                        )
       ]
       {:tilstand tilstand :funktionalitet funktionalitet :forfaedre {:gaengse-grafikmodul gaengse-grafikmodul}}
  )
)
