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
                           (.addPoint (new Point3D (+ x0-d 1) (+ y0-d 0) projection-plane-z (:top cell-frame-color)))
			   (.addPoint (new Point3D (- x1-d 0) (+ y0-d 0) projection-plane-z (:top cell-frame-color)))
			   (.addPoint (new Point3D (- x1-d 0) (- y0-d margin-y) projection-plane-z (:top cell-frame-color)))
			   (.addPoint (new Point3D (+ x0-d 1) (- y0-d margin-y) projection-plane-z (:top cell-frame-color)))
                   )
         cell-bottom (doto (new Polygon3D)
                           (.addPoint (new Point3D (+ x0-d 1) (+ y1-d 1) projection-plane-z (:bottom cell-frame-color)))
			   (.addPoint (new Point3D (- x1-d 0) (+ y1-d 1) projection-plane-z (:bottom cell-frame-color)))
			   (.addPoint (new Point3D (- x1-d 0) (+ y1-d margin-y) projection-plane-z (:bottom cell-frame-color)))
			   (.addPoint (new Point3D (+ x0-d 1) (+ y1-d margin-y) projection-plane-z (:bottom cell-frame-color)))
                     )
         cell-left (doto (new Polygon3D)
                           (.addPoint (new Point3D (+ x0-d 1) (- y0-d margin-y 1) projection-plane-z (:left cell-frame-color)))
			   (.addPoint (new Point3D (+ x0-d margin-x) (- y0-d margin-y 1) projection-plane-z (:left cell-frame-color)))
			   (.addPoint (new Point3D (+ x0-d margin-x) (+ y1-d margin-y 1) projection-plane-z (:left cell-frame-color)))
			   (.addPoint (new Point3D (+ x0-d 1) (+ y1-d margin-y 1) projection-plane-z (:left cell-frame-color)))
                     )
         cell-right (doto (new Polygon3D)
                           (.addPoint (new Point3D (- x1-d margin-x) (- y0-d margin-y 1) projection-plane-z (:right cell-frame-color)))
			   (.addPoint (new Point3D (- x1-d 0) (- y0-d margin-y 1) projection-plane-z (:right cell-frame-color)))
			   (.addPoint (new Point3D (- x1-d 0) (+ y1-d margin-y 1) projection-plane-z (:right cell-frame-color)))
			   (.addPoint (new Point3D (- x1-d margin-x) (+ y1-d margin-y 1) projection-plane-z (:right cell-frame-color)))
                     )
	 cell-chip (doto (new Polygon3D)
                           (.addPoint (new Point3D (+ x0-d margin-x 1) (- y0-d margin-y 1) projection-plane-z chip-color))
			   (.addPoint (new Point3D (- x1-d margin-x 1) (- y0-d margin-y 1) projection-plane-z chip-color))
			   (.addPoint (new Point3D (- x1-d margin-x 1) (+ y1-d margin-y 1) projection-plane-z chip-color))
			   (.addPoint (new Point3D (+ x0-d margin-x 1) (+ y1-d margin-y 1) projection-plane-z chip-color))
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

(defn generate-cell-grid-coords [board-width board-height base-frame]
  (let [
         base-frame-left-margin-pct 10
         base-frame-top-margin-pct 10
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

(defn update-scene-from-cell-coords [
                                      board
                                      camera
				      cell-coords
				      selected-cell-indexes
				      mouse-over-cell-indexes
				      mouse-over-cell-frame-color
				    ]
  (doseq [cell-coord cell-coords]
         (let [
	        cell-index {:row-index (:row-index cell-coord) :column-index (:column-index cell-coord)}
	        j (:column-index cell-coord)
		i (:row-index cell-coord)
		board-symbol (game-utils-misc/lookup board [j i])
		chip-color (if (= board-symbol "*")
			       Color/white
			       (if (= board-symbol "¤")
				   Color/red
				   Color/darkGray
			       )
			   )
		filtered-by-selection (filter #(and (= (:row-index %) i) (= (:column-index %) j)) selected-cell-indexes)
		selected? (> (count filtered-by-selection) 0)
		filtered-by-mouse-over (filter #(and (= (:row-index %) i) (= (:column-index %) j)) mouse-over-cell-indexes)
		mouse-over? (> (count filtered-by-mouse-over) 0)
		cell-frame-color (if mouse-over?
		                   mouse-over-cell-frame-color
				   (if selected?
				     {:top chip-color :bottom chip-color :left chip-color :right chip-color}
				     {:top Color/blue :bottom Color/blue :left Color/blue :right Color/blue}
				   )
				 )
	        cell-left-border (:cell-left-border cell-coord)
		cell-right-border (:cell-right-border cell-coord)
		cell-top-border (:cell-top-border cell-coord)
		cell-bottom-border (:cell-bottom-border cell-coord)
		board-cell-margin-pct 12
                board-cell (new-board-cell cell-left-border cell-top-border cell-right-border cell-bottom-border board-cell-margin-pct cell-frame-color chip-color)
	      ]
	      (doto camera
	        (.updateScene (:cell-top board-cell))
		(.updateScene (:cell-bottom board-cell))
		(.updateScene (:cell-left board-cell))
		(.updateScene (:cell-right board-cell))
		(.updateScene (:cell-chip board-cell))
	      )
	 )
  )
)

(defn gui-show-board [board camera base-frame cell-coords selected-cell-indexes]
  (let [
         base-frame-left-line (new Line3D (new Point3D (:base-frame-left-border base-frame) (:base-frame-top-border base-frame) projection-plane-z Color/red)
	                       (new Point3D (:base-frame-left-border base-frame) (:base-frame-bottom-border base-frame) projection-plane-z Color/red))
	 base-frame-top-line (new Line3D (new Point3D (:base-frame-left-border base-frame) (:base-frame-top-border base-frame) projection-plane-z Color/red)
	                      (new Point3D (:base-frame-right-border base-frame) (:base-frame-top-border base-frame) projection-plane-z Color/red))
	 base-frame-right-line (new Line3D (new Point3D (:base-frame-right-border base-frame) (:base-frame-top-border base-frame) projection-plane-z Color/red)
	                        (new Point3D (:base-frame-right-border base-frame) (:base-frame-bottom-border base-frame) projection-plane-z Color/red))
	 base-frame-bottom-line (new Line3D (new Point3D (:base-frame-right-border base-frame) (:base-frame-bottom-border base-frame) projection-plane-z Color/red)
	                         (new Point3D (:base-frame-left-border base-frame) (:base-frame-bottom-border base-frame) projection-plane-z Color/red))
       ]
       (doto camera
	 (.updateScene base-frame-left-line)
         (.updateScene base-frame-top-line)
	 (.updateScene base-frame-right-line)
	 (.updateScene base-frame-bottom-line)
       )
       (update-scene-from-cell-coords board camera cell-coords selected-cell-indexes [] nil)
       (.showScene camera)
  )
)

(defn transform-coords-in-mouse-event [camera insets mouse-event window-width window-height]
  (let [
         me-x (.getX mouse-event)
         me-y (.getY mouse-event)
	 insets-top (.top insets)
	 insets-left (.left insets)
	 me-w (+ me-x insets-left)
	 me-h (- me-y insets-top)
	 point-in-projection-plane (.getPointInProjectionPlaneFromPointOnScreen camera me-w me-h)
       ]
       {
         :transformed-x (.x point-in-projection-plane)
         :transformed-y (.y point-in-projection-plane)
       }
  )
)

(defn new-camera [window-width window-height]
  (new Camera 10.0 projection-plane-z window-width window-height window-width window-height)
)
