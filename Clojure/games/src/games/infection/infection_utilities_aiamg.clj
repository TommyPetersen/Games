(ns games.infection.infection-utilities-aiamg
  (:require (games [game-utilities-aiamg :as game-utils-aiamg])
            (games.infection [infection-utilities-misc :as infection-utils-misc]))
  (:import (java.awt Color))
  (:import (java.awt.event MouseEvent))
  (:import (java.awt Color)(Aiamg Camera Polygon3D Point3D Line3D))
)

(defn show-graphs [
                    camera			; Aiamg.Camera
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
                   (doto camera
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

(defn find-cell-coord [x y cell-coords]
  (let [
         pred #(and (>= x (:cell-left-border %)) (< x (:cell-right-border %))
                    (>= y (:cell-bottom-border %)) (< y (:cell-top-border %)))
	 cell-coord (first (filter pred cell-coords))
       ]
       cell-coord
  )
)

(defn get-user-selection ; {:from-cell from-cell :to-cell to-cell}
            		 [
      	                   board			; vec[kolonne-indeks 0...6][raekke-indeks 0...6]
	                   camera	  		; Aiamg.Camera
	                   window-width			; Klient-skaermens bredde i skaermpunkter, heltal stoerre end 0
	                   window-height	  	; Klient-skaermens hoejde i skaermpunkter, heltal stoerre end 0
	                   base-frame	  		; Til at tegne en kant udenom spilomraadet i klient-skaermen
	                   border-coords	       	; Spilomraadets graenser i klient-skaermen
	                   cell-coords	  		; Braettets celler udtrykt ved skaermkoordinater
	                   player-chip			; Spillerens brik udtrykt som tegnsymbol
			   sync-lock	  	     	; Clojure-lock som bruges til grafiksynkronisering
			   selected-cell-indexes      	; Atom til at kommunikere de valgte celler
			   mouse-over-cell-indexes	; Atom til at kommunikere de fokuserede celler
			   mouse-over-cell-frame-color  ; Atom til at kommunikere rammefarven paa de fokuserede celler
			   interrupt			; Atom til at afgoere om brugervalget skal afbrydes
            		 ]
  (let [
         valid-move-seq (infection-utils-misc/valid-move-seq board player-chip)
         insets (.getInsetsOnScreen camera)
	 find-cell-fn (fn []
	                (loop [
			        previous-selected-cell-index nil
			        previous-cell-coord-in-focus nil
	                        mouse-event (.getCurrentMouseEventOnScreen camera)
			        mouse-moved-event (.getCurrentMouseMovedEventOnScreen camera)
			      ]
			     (Thread/sleep 1)
			     (if @interrupt
			       {:from-cell {:row-index -1 :column-index -1} :to-cell {:row-index -1 :column-index -1}}
			     
                               (if (not= nil mouse-event)
			         (if (= (.getButton mouse-event) MouseEvent/BUTTON1)
			           (if (= nil previous-cell-coord-in-focus)
                                     (recur previous-selected-cell-index
			                    nil
				            (.getCurrentMouseEventOnScreen camera)
				            (.getCurrentMouseMovedEventOnScreen camera)
				     )
				     (if (= nil previous-selected-cell-index)
				       (do
				         (if (not @interrupt)
					   (do
				             (.lock sync-lock)
				             (try
					       (reset! mouse-over-cell-indexes nil)
					       (reset! mouse-over-cell-frame-color nil)
				               (reset! selected-cell-indexes [{:row-index (:row-index previous-cell-coord-in-focus) :column-index (:column-index previous-cell-coord-in-focus)}])
				               (game-utils-aiamg/gui-show-board board camera base-frame border-coords cell-coords @selected-cell-indexes)
				               (.showScene camera)
				               (finally
				                 (.unlock sync-lock)
				               )
				             )
					   )
					 )
				         (recur {:row-index (:row-index previous-cell-coord-in-focus) :column-index (:column-index previous-cell-coord-in-focus)}
				                nil
	                                        (.getCurrentMouseEventOnScreen camera)
			                        (.getCurrentMouseMovedEventOnScreen camera)
                                         )
				       )
				       (do
					 (if (not @interrupt)
					   (do
				             (.lock sync-lock)
				             (try
				               (reset! mouse-over-cell-indexes nil)
				               (reset! mouse-over-cell-frame-color nil)
				               (reset! selected-cell-indexes [previous-selected-cell-index {:row-index (:row-index previous-cell-coord-in-focus) :column-index (:column-index previous-cell-coord-in-focus)}])
				               (game-utils-aiamg/gui-show-board board camera base-frame border-coords cell-coords @selected-cell-indexes)
				               (.showScene camera)
				               (finally
				                 (.unlock sync-lock)
				               )
				             )
					   )
					 )
				         {:from-cell previous-selected-cell-index :to-cell {:row-index (:row-index previous-cell-coord-in-focus) :column-index (:column-index previous-cell-coord-in-focus)}}
				       )
				     )
				   )
				   (if (= (.getButton mouse-event) MouseEvent/BUTTON3)
				     (do
				       (if (not @interrupt)
				         (do
				           (.lock sync-lock)
				           (try
				             (reset! selected-cell-indexes nil)
				             (reset! mouse-over-cell-indexes nil)
				             (reset! mouse-over-cell-frame-color nil)
				             (game-utils-aiamg/gui-show-board board camera base-frame border-coords cell-coords @selected-cell-indexes)
				             (.showScene camera)
				             (finally
				               (.unlock sync-lock)
				             )
				           )
					 )
				       )
				       (recur nil
			                      previous-cell-coord-in-focus
	                                      (.getCurrentMouseEventOnScreen camera)
			                      (.getCurrentMouseMovedEventOnScreen camera)
				       )
				     )
				     (recur previous-selected-cell-index
			                    previous-cell-coord-in-focus
	                                    (.getCurrentMouseEventOnScreen camera)
			                    (.getCurrentMouseMovedEventOnScreen camera)
				     )
				   )
			         )
			       
	                         (if (not= nil mouse-moved-event)
	                           (let [
		                          transformed-coords (game-utils-aiamg/transform-coords-in-mouse-event camera insets mouse-moved-event window-width window-height)
			                  transformed-x (:transformed-x transformed-coords)
			                  transformed-y (:transformed-y transformed-coords)
		                        ]
		                        (if (and (>= transformed-x (double (:left border-coords))) (<= transformed-x (double (:right border-coords)))
		                                 (>= transformed-y (double (:bottom border-coords))) (<= transformed-y (double (:top border-coords)))
		                            )
				          (let [
					         cell-coord-in-focus (find-cell-coord transformed-x transformed-y cell-coords)
					         cell-index-in-focus {:row-index (:row-index cell-coord-in-focus) :column-index (:column-index cell-coord-in-focus)}
					         cell-coord-in-focus-is-valid? (and (not= nil cell-coord-in-focus)
					                                            (if (= nil previous-selected-cell-index)
									              (let [
									                     first-selection-pred #(and (= (first (:from-coord %)) (:column-index cell-index-in-focus))
										                                        (= (second (:from-coord %)) (:row-index cell-index-in-focus)))
										           ]
										           (boolean (some first-selection-pred valid-move-seq))
									              )
									              (let [
									                     second-selection-pred #(and (= (first (:from-coord %)) (:column-index previous-selected-cell-index))
										                                         (= (second (:from-coord %)) (:row-index previous-selected-cell-index))
									                                                 (= (first (:to-coord %)) (:column-index cell-index-in-focus))
										                                         (= (second (:to-coord %)) (:row-index cell-index-in-focus)))
										           ]
										           (boolean (some second-selection-pred valid-move-seq))
									              )
					                                            )
							                       )
					       ]
					       (if (not cell-coord-in-focus-is-valid?)
					         (do
					           (if (not= nil previous-cell-coord-in-focus)
					             (do
						       (if (not @interrupt)
						         (do
						           (.lock sync-lock)
						           (try
						             (reset! mouse-over-cell-indexes [{:row-index (:row-index previous-cell-coord-in-focus) :column-index (:column-index previous-cell-coord-in-focus)}])
						             (reset! mouse-over-cell-frame-color {:top Color/blue :bottom Color/blue :left Color/blue :right Color/blue})
						             (game-utils-aiamg/update-scene-from-cell-coords board camera [previous-cell-coord-in-focus] @selected-cell-indexes @mouse-over-cell-indexes @mouse-over-cell-frame-color)
						             (.showScene camera)
						             (finally
				                               (.unlock sync-lock)
				                             )
						           )
							 )
						       )
						     )
						   )
                                                   (recur previous-selected-cell-index
						          nil
					                  (.getCurrentMouseEventOnScreen camera)
					                  (.getCurrentMouseMovedEventOnScreen camera)
					           )
					         )
					         (if (= cell-coord-in-focus previous-cell-coord-in-focus)
                                                   (recur previous-selected-cell-index
						          previous-cell-coord-in-focus
					                  (.getCurrentMouseEventOnScreen camera)
					                  (.getCurrentMouseMovedEventOnScreen camera)
					           )
					           (do
						     (if (not @interrupt)
						       (do
						         (.lock sync-lock)
						         (try
						           (reset! mouse-over-cell-indexes [cell-index-in-focus])
						           (reset! mouse-over-cell-frame-color {:top Color/gray :bottom Color/gray :left Color/gray :right Color/gray})
					                   (if (not= nil previous-cell-coord-in-focus)(game-utils-aiamg/update-scene-from-cell-coords board camera [previous-cell-coord-in-focus] [previous-selected-cell-index] [{:row-index (:row-index previous-cell-coord-in-focus) :column-index (:column-index previous-cell-coord-in-focus)}] {:top Color/blue :bottom Color/blue :left Color/blue :right Color/blue}))
						           (game-utils-aiamg/update-scene-from-cell-coords board camera [cell-coord-in-focus] @selected-cell-indexes @mouse-over-cell-indexes @mouse-over-cell-frame-color)
						           (.showScene camera)
				                           (finally
				                             (.unlock sync-lock)
				                           )
							 )
						       )
					             )
                                                     (recur previous-selected-cell-index
						            cell-coord-in-focus
					                    (.getCurrentMouseEventOnScreen camera)
					                    (.getCurrentMouseMovedEventOnScreen camera)
					             )
					           )
					         )
					       )
	                                     )
					     (do
					       (if (not= nil previous-cell-coord-in-focus)
					         (do
						   (if (not @interrupt)
						     (do
					               (.lock sync-lock)
					               (try
						         (reset! mouse-over-cell-indexes [{:row-index (:row-index previous-cell-coord-in-focus) :column-index (:column-index previous-cell-coord-in-focus)}])
						         (reset! mouse-over-cell-frame-color {:top Color/blue :bottom Color/blue :left Color/blue :right Color/blue})
						         (game-utils-aiamg/update-scene-from-cell-coords board camera [previous-cell-coord-in-focus] @selected-cell-indexes @mouse-over-cell-indexes @mouse-over-cell-frame-color)
					                 (.showScene camera)
				                         (finally
				                           (.unlock sync-lock)
				                         )
					               )
						     )
						   )
					         )
					       )
                                               (recur previous-selected-cell-index
					              nil
					              (.getCurrentMouseEventOnScreen camera)
					              (.getCurrentMouseMovedEventOnScreen camera)
					       )
					     )
		                           )
		                         )
                                         (recur previous-selected-cell-index
				                previous-cell-coord-in-focus
				                (.getCurrentMouseEventOnScreen camera)
				                (.getCurrentMouseMovedEventOnScreen camera)
				         )
	                               )
			             )
			           )
                                 )
		               )
       ]
       (find-cell-fn)
  )
)

(defn get-user-move [
                      board
		      camera
		      window-width
		      window-height
		      base-frame
		      border-coords
		      cell-coords
		      player-chip
		      sync-lock
		      selected-cell-indexes
		      mouse-over-cell-indexes
		      mouse-over-cell-frame-color
		      interrupt
		    ]
  (let [
         selected-move (get-user-selection board camera window-width window-height base-frame border-coords cell-coords player-chip sync-lock selected-cell-indexes mouse-over-cell-indexes mouse-over-cell-frame-color interrupt)
       ]
       {
         :from-coord [(:column-index (:from-cell selected-move)) (:row-index (:from-cell selected-move))]
         :to-coord [(:column-index (:to-cell selected-move)) (:row-index (:to-cell selected-move))]
       }
  )
)
