(ns games.game-utilities-aiamg
  (:require (games [game-utilities-misc :as game-utils-misc]))
  (:import
	(java.awt Color)
	(Aiamg Camera Polygon3D Point3D Line3D)
  )
)

(def projection-plane-z 100.0)

(defn new-board-cell [x0 y0 x1 y1 margin-pct cell-frame-color chip-color]
  (let [
         x0-d (double x0) ;;; x0 could be like -324N which gives problems in Math/abs.
         x1-d (double x1)
         y0-d (double y0)
         y1-d (double y1)

         margin-x (Math/round (* (/ margin-pct 100) (Math/abs (- x1-d x0-d))))
         margin-y (Math/round (* (/ margin-pct 100) (Math/abs (- y0-d y1-d))))

         cell-top (doto (new Polygon3D)
                           (.addPoint (new Point3D (+ x0-d 0) (+ y0-d 0) projection-plane-z (:top cell-frame-color)))
			   (.addPoint (new Point3D (- x1-d 0) (+ y0-d 0) projection-plane-z (:top cell-frame-color)))
			   (.addPoint (new Point3D (- x1-d 0) (- y0-d margin-y) projection-plane-z (:top cell-frame-color)))
			   (.addPoint (new Point3D (+ x0-d 0) (- y0-d margin-y) projection-plane-z (:top cell-frame-color)))
                   )
         cell-bottom (doto (new Polygon3D)
                           (.addPoint (new Point3D (+ x0-d 0) (+ y1-d 0) projection-plane-z (:bottom cell-frame-color)))
			   (.addPoint (new Point3D (- x1-d 0) (+ y1-d 0) projection-plane-z (:bottom cell-frame-color)))
			   (.addPoint (new Point3D (- x1-d 0) (+ y1-d margin-y) projection-plane-z (:bottom cell-frame-color)))
			   (.addPoint (new Point3D (+ x0-d 0) (+ y1-d margin-y) projection-plane-z (:bottom cell-frame-color)))
                     )
         cell-left (doto (new Polygon3D)
                           (.addPoint (new Point3D (+ x0-d 0) (- y0-d margin-y 0) projection-plane-z (:left cell-frame-color)))
			   (.addPoint (new Point3D (+ x0-d margin-x) (- y0-d margin-y 0) projection-plane-z (:left cell-frame-color)))
			   (.addPoint (new Point3D (+ x0-d margin-x) (+ y1-d margin-y 0) projection-plane-z (:left cell-frame-color)))
			   (.addPoint (new Point3D (+ x0-d 0) (+ y1-d margin-y 0) projection-plane-z (:left cell-frame-color)))
                     )
         cell-right (doto (new Polygon3D)
                           (.addPoint (new Point3D (- x1-d margin-x) (- y0-d margin-y 0) projection-plane-z (:right cell-frame-color)))
			   (.addPoint (new Point3D (- x1-d 0) (- y0-d margin-y 0) projection-plane-z (:right cell-frame-color)))
			   (.addPoint (new Point3D (- x1-d 0) (+ y1-d margin-y 0) projection-plane-z (:right cell-frame-color)))
			   (.addPoint (new Point3D (- x1-d margin-x) (+ y1-d margin-y 0) projection-plane-z (:right cell-frame-color)))
                     )
	 cell-chip (doto (new Polygon3D)
                           (.addPoint (new Point3D (+ x0-d margin-x 1) (- y0-d margin-y 0) projection-plane-z chip-color))
			   (.addPoint (new Point3D (- x1-d margin-x 1) (- y0-d margin-y 0) projection-plane-z chip-color))
			   (.addPoint (new Point3D (- x1-d margin-x 1) (+ y1-d margin-y 0) projection-plane-z chip-color))
			   (.addPoint (new Point3D (+ x0-d margin-x 1) (+ y1-d margin-y 0) projection-plane-z chip-color))
                   )
       ]
       {:cell-top cell-top :cell-bottom cell-bottom :cell-left cell-left :cell-right cell-right :cell-chip cell-chip}
  )
)

(defn calculate-base-frame [window-width window-height]
  (let [
         window-left-border (* -1 (/ window-width 2))
         window-right-border (/ window-width 2)
         window-bottom-border (* -1 (/ window-height 2))
         window-top-border (/ window-height 2)

         window-top-margin-pct 10
         window-bottom-margin-pct 10
         window-left-margin-pct 10
         window-right-margin-pct 10
       ]
       {
         :base-frame-left-border (* window-left-border (/ (- 100 window-left-margin-pct) 100))
         :base-frame-top-border (* window-top-border (/ (- 100 window-top-margin-pct) 100))
         :base-frame-right-border (* window-right-border (/ (- 100 window-right-margin-pct) 100))
         :base-frame-bottom-border (* window-bottom-border (/ (- 100 window-bottom-margin-pct) 100))
       }
  )
)

(defn calculate-aux-frame [
                            frame-border-left		; I skaermpunkter (pixels)
			    frame-border-right
			    frame-border-top
			    frame-border-bottom
				
                            nedtaellingsramme-venstre-margin-pctr ; [50 10 85 0]
			  ]
  (let [
         frame-margin-pct-left (nth nedtaellingsramme-venstre-margin-pctr 0)	; I procent, mellem 0 og 100
	 frame-margin-pct-right (nth nedtaellingsramme-venstre-margin-pctr 1)
	 frame-margin-pct-top (nth nedtaellingsramme-venstre-margin-pctr 2)
	 frame-margin-pct-bottom (nth nedtaellingsramme-venstre-margin-pctr 3)
       ]
       {
         :frame-x0 (+ frame-border-left (* (- frame-border-right frame-border-left) (/ frame-margin-pct-left 100)))
         :frame-y0 (+ frame-border-bottom (* (- frame-border-top frame-border-bottom) (/ frame-margin-pct-bottom 100)))
         :frame-x1 (- frame-border-right (* (- frame-border-right frame-border-left) (/ frame-margin-pct-right 100)))
         :frame-y1 (- frame-border-top (* (- frame-border-top frame-border-bottom) (/ frame-margin-pct-top 100)))
       }
  )
)

(defn show-aux-frame [
                       kamera		; Aiamg.Camera
                       aux-frame	; {:frame-x0 :frame-y0 :frame-x1 :frame-y1}
		     ]
  (let [
         frame-color Color/gray
         aux-frame-left-line (new Line3D (new Point3D (:frame-x0 aux-frame) (:frame-y0 aux-frame) projection-plane-z frame-color)
	                                 (new Point3D (:frame-x0 aux-frame) (:frame-y1 aux-frame) projection-plane-z frame-color))
	 aux-frame-top-line (new Line3D (new Point3D (:frame-x0 aux-frame) (:frame-y1 aux-frame) projection-plane-z frame-color)
	                                (new Point3D (:frame-x1 aux-frame) (:frame-y1 aux-frame) projection-plane-z frame-color))
	 aux-frame-right-line (new Line3D (new Point3D (:frame-x1 aux-frame) (:frame-y0 aux-frame) projection-plane-z frame-color)
	                                  (new Point3D (:frame-x1 aux-frame) (:frame-y1 aux-frame) projection-plane-z frame-color))
	 aux-frame-bottom-line (new Line3D (new Point3D (:frame-x0 aux-frame) (:frame-y0 aux-frame) projection-plane-z frame-color)
	                                   (new Point3D (:frame-x1 aux-frame) (:frame-y0 aux-frame) projection-plane-z frame-color))
       ]
       (doto kamera
	 (.updateScene aux-frame-left-line)
         (.updateScene aux-frame-top-line)
	 (.updateScene aux-frame-right-line)
	 (.updateScene aux-frame-bottom-line)
       )
  )
)

(defn vis-nedtaelling [
                        kamera			; Aiamg.Camera
		        spillerbrik		; Spiller1: "*", Spiller2: "¤"
		        samlet-tidsforbrug	; Det samlede tidsforbrug
		        tidsgraense		; Tidsgraensen
		        nedtaellingsramme-x0	; Nedtaellingsrammens origo-x
		        nedtaellingsramme-y0	; Nedtaellingsrammens origo-y
    		        nedtaellingsramme-x1	; Nedtaellingsrammens hoejre graense
		        nedtaellingsramme-y1	; Nedtaellingsrammens oevre graense
                      ]
  (if (< samlet-tidsforbrug tidsgraense)
    (let [
 	   brikfarve (if (= spillerbrik "*") Color/white Color/red)
           broekdel (/ samlet-tidsforbrug tidsgraense)
	   yr (+ (* nedtaellingsramme-y1 (- 1 broekdel)) (* nedtaellingsramme-y0 broekdel))
	   yr-oevre-graense (if (> yr nedtaellingsramme-y1) (- yr 1) yr)
	   nedtaellingsfyld (doto (new Polygon3D)
                                  (.addPoint (new Point3D nedtaellingsramme-x0 nedtaellingsramme-y0 projection-plane-z brikfarve))
                                  (.addPoint (new Point3D nedtaellingsramme-x1 nedtaellingsramme-y0 projection-plane-z brikfarve))
                                  (.addPoint (new Point3D nedtaellingsramme-x1 yr-oevre-graense projection-plane-z brikfarve))
                                  (.addPoint (new Point3D nedtaellingsramme-x0 yr-oevre-graense projection-plane-z brikfarve))
                            )
         ]
         (doto kamera
	   (.updateScene nedtaellingsfyld)
         )
    )
  )
)

(defn generate-cell-grid-coords [
                                  board-width
				  board-height
				  base-frame
				]
  (let [
         base-frame-left-margin-pct 10
         base-frame-top-margin-pct 30
         base-frame-right-margin-pct 10
         base-frame-bottom-margin-pct 10
	 
         cell-grid-left-border-x (* (:base-frame-left-border base-frame) (/ (- 100 base-frame-left-margin-pct) 100))
         cell-grid-right-border-x (* (:base-frame-right-border base-frame) (/ (- 100 base-frame-right-margin-pct) 100))
         cell-grid-bottom-border-y (* (:base-frame-bottom-border base-frame) (/ (- 100 base-frame-bottom-margin-pct) 100))
         cell-grid-top-border-y (* (:base-frame-top-border base-frame) (/ (- 100 base-frame-top-margin-pct) 100))

	 cell-grid-row-spacing 0
	 cell-grid-column-spacing 0
	 
	 cell-grid-delta-x (/ (- (- cell-grid-right-border-x cell-grid-left-border-x) (* (- board-width 1) cell-grid-column-spacing)) board-width)
	 cell-grid-delta-y (/ (- (- cell-grid-top-border-y cell-grid-bottom-border-y) (* (- board-height 1) cell-grid-row-spacing)) board-height)

         generate-cell-coords-row-i (fn [i y0]
	                              (loop [
	                                      j 0
	                                      x0 cell-grid-left-border-x
		                              cell-coords-row [{:row-index i :cell-top-border y0 :cell-bottom-border (- y0 cell-grid-delta-y)
				                                :column-index j :cell-left-border x0 :cell-right-border (+ x0 cell-grid-delta-x)}]
	                                    ]
	                                    (if (< j (- board-width 1))
			                        (recur (+ j 1)
			                               (+ x0 cell-grid-delta-x cell-grid-column-spacing)
					               (conj cell-coords-row {:row-index i
						                              :cell-top-border y0
					                                      :cell-bottom-border (- y0 cell-grid-delta-y)
				                                              :column-index (+ j 1)
						  		              :cell-left-border (+ x0 cell-grid-delta-x cell-grid-column-spacing)
								              :cell-right-border (+ x0 cell-grid-delta-x cell-grid-column-spacing cell-grid-delta-x)
	                                                                     }
					               )
		                                )
			                        cell-coords-row
			                    )
	                              )
	                            )
       ]
       (let [cell-coords (loop [
                                 i (- board-height 1)
                                 y0 cell-grid-top-border-y
	                         cell-coords-rows []
	                       ]
	                       (let [
			              cell-coords-row-i (generate-cell-coords-row-i i y0)
				    ]
	                            (if (> i 0)
	                                (recur
					  (- i 1)
					  (- y0 cell-grid-delta-y cell-grid-row-spacing)
					  (concat cell-coords-rows cell-coords-row-i)
					)
		                        (concat cell-coords-rows cell-coords-row-i)
	                            )
	                      )
                         )
            ]
	    {
	      :border-coords {
	                       :left cell-grid-left-border-x
                               :right cell-grid-right-border-x
                               :bottom cell-grid-bottom-border-y
                               :top cell-grid-top-border-y
			     }
	      :cell-coords cell-coords
	    }
     )
  )
)

(defn find-cell-coord [x y cell-coords]
  (let [
         pred #(and (>= x (:cell-left-border %)) (< x (:cell-right-border %))
                    (>= y (:cell-bottom-border %)) (< y (:cell-top-border %)))
	 cell-coord (first (filter pred cell-coords))
       ]
       cell-coord
  )
)

(defn update-scene-from-cell-coords [
                                      board
                                      kamera
				      cell-coords
				      selected-cell-indexes
				      mouse-over-cell-index
				      mouse-over-cell-frame-color
				    ]
  (doseq [cell-coord cell-coords]
         (let [
	        cell-index {:row-index (:row-index cell-coord) :column-index (:column-index cell-coord)}
	        j (:column-index cell-coord)
		i (:row-index cell-coord)
		board-symbol (game-utils-misc/lookup board [j i])
		brikfarve (if (= board-symbol "*")
			       Color/white
			       (if (= board-symbol "¤")
				   Color/red
				   Color/darkGray
			       )
			   )
		filtered-by-selection (filter #(and (= (:row-index %) i) (= (:column-index %) j)) selected-cell-indexes)
		selected? (> (count filtered-by-selection) 0)
		mouse-over? (and (= (:row-index mouse-over-cell-index) i) (= (:column-index mouse-over-cell-index) j))
		cell-frame-color (if mouse-over?
		                   mouse-over-cell-frame-color
		                   (if selected?
				     {:top brikfarve :bottom brikfarve :left brikfarve :right brikfarve}
				     {:top Color/blue :bottom Color/blue :left Color/blue :right Color/blue}
				   )
				 )
	        cell-left-border (:cell-left-border cell-coord)
		cell-right-border (:cell-right-border cell-coord)
		cell-top-border (:cell-top-border cell-coord)
		cell-bottom-border (:cell-bottom-border cell-coord)
		board-cell-margin-pct 12
                board-cell (new-board-cell cell-left-border cell-top-border cell-right-border cell-bottom-border board-cell-margin-pct cell-frame-color brikfarve)
	      ]
	      (doto kamera
	        (.updateScene (:cell-top board-cell))
		(.updateScene (:cell-bottom board-cell))
		(.updateScene (:cell-left board-cell))
		(.updateScene (:cell-right board-cell))
		(.updateScene (:cell-chip board-cell))
	      )
	 )
  )
)

(defn gui-show-board [
                       board
		       kamera
		       base-frame
		       border-coords
		       cell-coords
		       selected-cell-indexes
		       mouse-over-cell-index
		       mouse-over-cell-frame-color
		     ]
  (let [
         base-frame-left-line (new Line3D (new Point3D (:base-frame-left-border base-frame) (:base-frame-top-border base-frame) projection-plane-z Color/red)
	                       (new Point3D (:base-frame-left-border base-frame) (:base-frame-bottom-border base-frame) projection-plane-z Color/red))
	 base-frame-top-line (new Line3D (new Point3D (:base-frame-left-border base-frame) (:base-frame-top-border base-frame) projection-plane-z Color/red)
	                      (new Point3D (:base-frame-right-border base-frame) (:base-frame-top-border base-frame) projection-plane-z Color/red))
	 base-frame-right-line (new Line3D (new Point3D (:base-frame-right-border base-frame) (:base-frame-top-border base-frame) projection-plane-z Color/red)
	                        (new Point3D (:base-frame-right-border base-frame) (:base-frame-bottom-border base-frame) projection-plane-z Color/red))
	 base-frame-bottom-line (new Line3D (new Point3D (:base-frame-right-border base-frame) (:base-frame-bottom-border base-frame) projection-plane-z Color/red)
	                         (new Point3D (:base-frame-left-border base-frame) (:base-frame-bottom-border base-frame) projection-plane-z Color/red))

         cell-frame-left-line (new Line3D (new Point3D (- (:left border-coords) 1) (+ (:top border-coords) 1) projection-plane-z Color/gray)
	                                  (new Point3D (- (:left border-coords) 1) (- (:bottom border-coords) 1) projection-plane-z Color/gray))
	 cell-frame-top-line (new Line3D (new Point3D (- (:left border-coords) 1) (+ (:top border-coords) 1) projection-plane-z Color/gray)
	                                 (new Point3D (+ (:right border-coords) 1) (+ (:top border-coords) 1) projection-plane-z Color/gray))
	 cell-frame-right-line (new Line3D (new Point3D (+ (:right border-coords) 1) (+ (:top border-coords) 1) projection-plane-z Color/gray)
	                                   (new Point3D (+ (:right border-coords) 1) (- (:bottom border-coords) 1) projection-plane-z Color/gray))
	 cell-frame-bottom-line (new Line3D (new Point3D (+ (:right border-coords) 1) (- (:bottom border-coords) 1) projection-plane-z Color/gray)
	                                    (new Point3D (- (:left border-coords) 1) (- (:bottom border-coords) 1) projection-plane-z Color/gray))
       ]
       (doto kamera
	 (.updateScene base-frame-left-line)
         (.updateScene base-frame-top-line)
	 (.updateScene base-frame-right-line)
	 (.updateScene base-frame-bottom-line)
	 (.updateScene cell-frame-left-line)
	 (.updateScene cell-frame-top-line)
	 (.updateScene cell-frame-right-line)
	 (.updateScene cell-frame-bottom-line)
       )
       (update-scene-from-cell-coords board kamera cell-coords selected-cell-indexes mouse-over-cell-index mouse-over-cell-frame-color)
  )
)

(defn transform-coords-in-mouse-event [kamera insets mouse-event]
  (if (= nil mouse-event)
    {
      :transformed-x nil
      :transformed-y nil
    }
    (let [
           me-x (.getX mouse-event)
           me-y (.getY mouse-event)
	   insets-top (.top insets)
	   insets-left (.left insets)
	   me-w (+ me-x insets-left)
	   me-h (- me-y insets-top)
	   point-in-projection-plane (.getPointInProjectionPlaneFromPointOnScreen kamera me-w me-h)
         ]
         {
           :transformed-x (.x point-in-projection-plane)
           :transformed-y (.y point-in-projection-plane)
         }
    )
  )
)

(defn nyt-kamera [window-width window-height]
  (new Camera 10.0 projection-plane-z window-width window-height window-width window-height)
)

;;; Grafikmodul ;;;

(def grafikmodul
  (let [
         synkroniseringsref (ref {
	                           :punktgitter nil
				   :vis-scene nil
				 }
		            )
         tilstand (ref {
	                 :braet nil
	                 :spillerbrik nil
			 :modstanderbrik nil
                         :kamera nil
		         :base-frame nil
		         :cell-grid-coords nil
                         :valgte-celler-indekseret nil
                         :fokuseret-celle-indekseret nil
                         :fokuseret-celle-rammefarve nil
                         :nedtaellingsramme-spiller nil
                         :nedtaellingsramme-modstander nil
			 :samlet-tid-for-spiller nil
			 :samlet-tid-for-modstander nil
			 :tidsgraense nil
                       }
		  )
         funktionalitet (atom {
	                        :fastsaet-tilstand (fn [
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
						     (let [
						            spillerbrik (if (= spillernummer 1) "*" "¤")
							    modstanderbrik (if (= spillernummer 1) "¤" "*")
						            kamera (nyt-kamera vinduesbredde vindueshoejde)
							    base-frame (calculate-base-frame vinduesbredde vindueshoejde)
							    cell-grid-coords (generate-cell-grid-coords braetbredde braethoejde base-frame)
							    border-coords (:border-coords cell-grid-coords)
							    cell-coords (:cell-coords cell-grid-coords)
							    nedtaellingsramme-venstre (calculate-aux-frame (:base-frame-left-border base-frame) (:left border-coords) (:top border-coords) (- (:bottom border-coords) 1) nedtaellingsramme-venstre-margin-pctr)
							    nedtaellingsramme-hoejre (calculate-aux-frame (:right border-coords) (:base-frame-right-border base-frame) (:top border-coords) (- (:bottom border-coords) 1) nedtaellingsramme-hoejre-margin-pctr)
							    nedtaellingsramme-spiller (if (= spillernummer 1) nedtaellingsramme-venstre nedtaellingsramme-hoejre)
							    nedtaellingsramme-modstander (if (= spillernummer 1) nedtaellingsramme-hoejre nedtaellingsramme-venstre)
							    samlet-tid-for-spiller 0
							    samlet-tid-for-modstander 0
						          ]
							  (dosync
						            (alter tilstand assoc :braet braet
							                          :spillerbrik spillerbrik
							  		          :modstanderbrik modstanderbrik
							                          :kamera kamera
								                  :base-frame base-frame
								                  :cell-grid-coords cell-grid-coords
								  	          :valgte-celler-indekseret []
									          :fokuseret-celle-indekseret nil
									          :fokuseret-celle-rammefarve nil
									          :nedtaellingsramme-spiller nedtaellingsramme-spiller
									          :nedtaellingsramme-modstander nedtaellingsramme-modstander
									          :samlet-tid-for-spiller samlet-tid-for-spiller
									          :samlet-tid-for-modstander samlet-tid-for-modstander
									          :tidsgraense tidsgraense
						            )
							  )
						     )
					           )
				:kald-funktion-med-laas (fn [
				                              funktion
				                              argumentliste
			                                    ]
							  (dosync (apply funktion argumentliste))
							)
				:opdater-tilstand (fn [
				                        noegle
							vaerdi
						      ]
						    (dosync (alter tilstand assoc noegle vaerdi))
				                  )
				:vaelg-fokuseret-celle (fn []
				                         (let [
					                        valgte-celler-indekseret (@tilstand :valgte-celler-indekseret)
						                opdaterede-valgte-celler-indekseret (if (= (@tilstand :fokuseret-celle-indekseret) nil)
						                                                      valgte-celler-indekseret
						                                                      (conj valgte-celler-indekseret (@tilstand :fokuseret-celle-indekseret))
											            )
					                      ]
							      (dosync (alter tilstand assoc :valgte-celler-indekseret opdaterede-valgte-celler-indekseret))
					                 )
				                       )
				:hent-valgte-celler (fn [] (@tilstand :valgte-celler-indekseret))
				:fravaelg-alle-valgte-celler (fn [] (dosync (alter tilstand assoc :valgte-celler-indekseret [])))
				:rens-laerred (fn [] (dosync (alter synkroniseringsref assoc :punktgitter (.clearRaster (@tilstand :kamera)))))
				:vis-laerred (fn [] (dosync (alter synkroniseringsref assoc :vis-scene (.showScene (@tilstand :kamera)))))
				:tegn-de-gaengse-figurer (fn []
				                             (let [
							            spillerbrik (@tilstand :spillerbrik)
								    modstanderbrik (@tilstand :modstanderbrik)
								    samlet-tid-for-spiller (@tilstand :samlet-tid-for-spiller)
								    samlet-tid-for-modstander (@tilstand :samlet-tid-for-modstander)
								    tidsgraense (@tilstand :tidsgraense)
							            braet (@tilstand :braet)
				                                    border-coords ((@tilstand :cell-grid-coords) :border-coords)
					                            cell-coords ((@tilstand :cell-grid-coords) :cell-coords)
						                    kamera (@tilstand :kamera)
						                    nedtaellingsramme-spiller (@tilstand :nedtaellingsramme-spiller)
						                    nedtaellingsramme-modstander (@tilstand :nedtaellingsramme-modstander)
				                                  ]
			                                          (gui-show-board braet kamera (@tilstand :base-frame) border-coords cell-coords (@tilstand :valgte-celler-indekseret) (@tilstand :fokuseret-celle-indekseret) (@tilstand :fokuseret-celle-rammefarve))
		         			                  (show-aux-frame kamera nedtaellingsramme-spiller)
		         			                  (show-aux-frame kamera nedtaellingsramme-modstander)
						                  (vis-nedtaelling kamera spillerbrik samlet-tid-for-spiller tidsgraense (+ (:frame-x0 nedtaellingsramme-spiller) 1) (+ (:frame-y0 nedtaellingsramme-spiller) 1) (- (:frame-x1 nedtaellingsramme-spiller) 1) (- (:frame-y1 nedtaellingsramme-spiller) 1))
                                                                  (vis-nedtaelling kamera modstanderbrik samlet-tid-for-modstander tidsgraense (+ (:frame-x0 nedtaellingsramme-modstander) 1) (+ (:frame-y0 nedtaellingsramme-modstander) 1) (- (:frame-x1 nedtaellingsramme-modstander) 1) (- (:frame-y1 nedtaellingsramme-modstander) 1))
				                             )
			                                 )

                              }
			)
       ]
       {:tilstand tilstand :funktionalitet funktionalitet}
  )
)
