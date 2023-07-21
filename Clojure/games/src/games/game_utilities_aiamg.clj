(ns games.game-utilities-aiamg
  (:require (games [game-utilities-misc :as game-utils-misc]))
  (:import
	(java.awt Color)
	(Aiamg Camera Polygon3D Point3D Line3D)
  )
)

(def projection-plane-z 200.0)

(defn new-board-cell [x0 y0 x1 y1 margin-pct cell-color chip-color]
  (let [
         x0-d (double x0) ;;; x0 could be like -324N which gives problems in Math/abs.
         x1-d (double x1)
         y0-d (double y0)
         y1-d (double y1)

         margin-x (Math/round (* (/ margin-pct 100) (Math/abs (- x1-d x0-d))))
         margin-y (Math/round (* (/ margin-pct 100) (Math/abs (- y0-d y1-d))))

         x0-i (+ x0 1)
         x1-d (- x1 1)
         y0-d (- y0 1)
         y1-i (+ y1 1)

         cell-top (doto (new Polygon3D)
                           (.addPoint (new Point3D x0 y0 projection-plane-z cell-color))
			   (.addPoint (new Point3D x1 y0 projection-plane-z cell-color))
			   (.addPoint (new Point3D x1 (- y0 margin-y) projection-plane-z cell-color))
			   (.addPoint (new Point3D x0 (- y0 margin-y) projection-plane-z cell-color))
                   )
         cell-bottom (doto (new Polygon3D)
                           (.addPoint (new Point3D x0 y1 projection-plane-z cell-color))
			   (.addPoint (new Point3D x1 y1 projection-plane-z cell-color))
			   (.addPoint (new Point3D x1 (+ y1 margin-y) projection-plane-z cell-color))
			   (.addPoint (new Point3D x0 (+ y1 margin-y) projection-plane-z cell-color))
                     )
         cell-left (doto (new Polygon3D)
                           (.addPoint (new Point3D x0 (- y0 margin-y) projection-plane-z cell-color))
			   (.addPoint (new Point3D (+ x0 margin-x) (- y0 margin-y) projection-plane-z cell-color))
			   (.addPoint (new Point3D (+ x0 margin-x) (+ y1 margin-y) projection-plane-z cell-color))
			   (.addPoint (new Point3D x0 (+ y1 margin-y) projection-plane-z cell-color))
                     )
         cell-right (doto (new Polygon3D)
                           (.addPoint (new Point3D (- x1 margin-x) (- y0 margin-y) projection-plane-z cell-color))
			   (.addPoint (new Point3D x1 (- y0 margin-y) projection-plane-z cell-color))
			   (.addPoint (new Point3D x1 (+ y1 margin-y) projection-plane-z cell-color))
			   (.addPoint (new Point3D (- x1 margin-x) (+ y1 margin-y) projection-plane-z cell-color))
                     )
	 cell-chip (doto (new Polygon3D)
                           (.addPoint (new Point3D (+ x0 margin-x) (- y0 margin-y) projection-plane-z chip-color))
			   (.addPoint (new Point3D (- x1 margin-x) (- y0 margin-y) projection-plane-z chip-color))
			   (.addPoint (new Point3D (- x1 margin-x) (+ y1 margin-y) projection-plane-z chip-color))
			   (.addPoint (new Point3D (+ x0 margin-x) (+ y1 margin-y) projection-plane-z chip-color))
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
         base-frame-top-margin-pct 30
         base-frame-right-margin-pct 10
         base-frame-bottom-margin-pct 30
	 
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
					               (conj cell-coords-row {:row-index i :cell-top-border y0
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
	                       (let [cell-coords-row-i (generate-cell-coords-row-i i y0)]
	                            (if (> i 0)
	                                (recur (- i 1) (- y0 cell-grid-delta-y cell-grid-row-spacing) (concat cell-coords-rows cell-coords-row-i))
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

(defn gui-show-board [board camera base-frame cell-coords selected-cell-coords]
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
       (doseq [cell-coord cell-coords]
         (let [
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
		filtered-selection (filter #(and (= (:row-index %) i) (= (:column-index %) j)) selected-cell-coords)
		selected? (> (count filtered-selection) 0)
		cell-color (if selected? chip-color Color/blue)
	        cell-left-border (:cell-left-border cell-coord)
		cell-right-border (:cell-right-border cell-coord)
		cell-top-border (:cell-top-border cell-coord)
		cell-bottom-border (:cell-bottom-border cell-coord)
		board-cell-margin-pct 12
                board-cell (new-board-cell cell-left-border cell-top-border cell-right-border cell-bottom-border board-cell-margin-pct cell-color chip-color)
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
       (.showScene camera)
  )
)

(defn transform-coords-in-mouse-event [insets mouse-event window-width window-height]
  (let [
         me-x (.getX mouse-event)
         me-y (.getY mouse-event)
	 insets-top (.top insets)
	 insets-left (.left insets)
       ]
       {
         :transformed-x (- (+ me-x insets-left) (/ window-width 2))
         :transformed-y (- (/ window-height 2) (- me-y insets-top))
       }
  )
)

(defn new-camera [window-width window-height]
  (new Camera 10.0 projection-plane-z window-width window-height window-width window-height)
)
