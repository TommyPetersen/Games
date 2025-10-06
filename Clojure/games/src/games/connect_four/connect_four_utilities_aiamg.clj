(ns games.connect-four.connect-four-utilities-aiamg
  (:require (games [game-utilities-aiamg :as game-utils-aiamg]))
  (:require (games.connect-four [connect-four-utilities-misc :as connect-four-utils-misc]))
  (:import (java.awt Color))
  (:import (java.awt.event MouseEvent))
)

(defn find-cell-coord [
                        x
			y
			cell-coords
		      ]
  (let [
         pred #(and (>= x (:cell-left-border %)) (< x (:cell-right-border %))
                    (>= y (:cell-bottom-border %)) (< y (:cell-top-border %)))
	 cell-coord (first (filter pred cell-coords))
       ]
       cell-coord
  )
)

(defn get-user-move [
                      board				; vec[kolonne-indeks 0...6][raekke-indeks 0...5]
		      camera				; Aiamg.Camera
		      window-width			; Klient-skaermens bredde i skaermpunkter, heltal stoerre end 0
		      window-height			; Klient-skaermens hoejde i skaermpunkter, heltal stoerre end 0
		      border-coords			; Spilomraadets graenser i klient-skaermen
		      cell-coords			; Braettets celler udtrykt ved skaermkoordinater
		      sync-lock	  	     		; Clojure-lock som bruges til grafiksynkronisering
		      selected-cell-indexes		; Atom til at kommunikere de valgte celler
		      mouse-over-cell-indexes		; Atom til at kommunikere de fokuserede celler
		      mouse-over-cell-frame-color  	; Atom til at kommunikere rammefarven paa de fokuserede celler
		      interrupt				; Atom til at afgoere om brugervalget skal afbrydes
		    ]
  (let [
         valid-column-seq (connect-four-utils-misc/valid-column-seq board 6)
         insets (.getInsetsOnScreen camera)
       ]
       (loop [
	       previous-cell-coord-in-focus nil
	       mouse-event (.getCurrentMouseEventOnScreen camera)
	       mouse-moved-event (.getCurrentMouseMovedEventOnScreen camera)
	     ]
	     (Thread/sleep 1)
             (if @interrupt
	       -1
	       (if (and (not= nil mouse-event) (= (.getButton mouse-event) MouseEvent/BUTTON1))
	         (if (= nil previous-cell-coord-in-focus)
                   (recur previous-cell-coord-in-focus
	                  (.getCurrentMouseEventOnScreen camera)
			  (.getCurrentMouseMovedEventOnScreen camera))
		   (do
		     (.lock sync-lock)
		     (try
		       (reset! mouse-over-cell-indexes nil)
		       (reset! mouse-over-cell-frame-color nil)
		       (finally (.unlock sync-lock))
		     )
	             (:column-index previous-cell-coord-in-focus)
		   )
		 )
                 (if (not= nil mouse-moved-event)
	           (let [
		          transformed-coords (game-utils-aiamg/transform-coords-in-mouse-event camera insets mouse-moved-event window-width window-height)
			  transformed-x (:transformed-x transformed-coords)
			  transformed-y (:transformed-y transformed-coords)
			]
		        (if (and (>= transformed-x (double (:left border-coords))) (<= transformed-x (double (:right border-coords)))
		                 (>= transformed-y (double (:bottom border-coords))) (<= transformed-y (double (:top border-coords))))
			  (let [
			         cell-coord-in-focus (find-cell-coord transformed-x transformed-y cell-coords)
				 cell-index-in-focus {:row-index (:row-index cell-coord-in-focus) :column-index (:column-index cell-coord-in-focus)}
				 cell-coord-in-focus-is-valid? (and (not= nil cell-coord-in-focus)
				                                    (let [
				                                           selection-pred #(= (:column-index cell-coord-in-focus) %)]
								         (boolean (some selection-pred valid-column-seq))))
			       ]
			       (if (not cell-coord-in-focus-is-valid?)
			         (do
				   (if (not= nil previous-cell-coord-in-focus)
		   		     (let [
				            prev-i (:row-index previous-cell-coord-in-focus)
					    prev-j (:column-index previous-cell-coord-in-focus)
					    prev-filtered-by-selection (filter #(and (= (:row-index %) prev-i) (= (:column-index %) prev-j)) @selected-cell-indexes)
					    prev-selected? (> (count prev-filtered-by-selection) 0)
					  ]
					  (if (not @interrupt)
					    (do
					      (reset! mouse-over-cell-indexes nil)
					      (reset! mouse-over-cell-frame-color nil)
					      (.lock sync-lock)
					      (try
				     	        (if (not prev-selected?)
					          (game-utils-aiamg/update-scene-from-cell-coords board camera [previous-cell-coord-in-focus]
					  	                                                  @selected-cell-indexes
						 		                                  [{:row-index (:row-index previous-cell-coord-in-focus)
											            :column-index (:column-index previous-cell-coord-in-focus)}]
											          {:top Color/blue :bottom Color/blue
											           :left Color/blue :right Color/blue})
					          (game-utils-aiamg/update-scene-from-cell-coords board camera [previous-cell-coord-in-focus]
					                                                          @selected-cell-indexes
										                  nil
											          nil)
					        )
					        (.showScene camera)
				    	        (finally
				                  (.unlock sync-lock)
				                )
				              )
					    )
					  )
			             )
				   )
				   (recur nil
				          (.getCurrentMouseEventOnScreen camera)
				          (.getCurrentMouseMovedEventOnScreen camera)
				   )
                                 )

                                 (if (= cell-coord-in-focus previous-cell-coord-in-focus)
                                   (recur previous-cell-coord-in-focus
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
				           (if (not= nil previous-cell-coord-in-focus)
				             (let [
					            prev-i (:row-index previous-cell-coord-in-focus)
						    prev-j (:column-index previous-cell-coord-in-focus)
					            prev-filtered-by-selection (filter #(and (= (:row-index %) prev-i) (= (:column-index %) prev-j)) @selected-cell-indexes)
						    prev-selected? (> (count prev-filtered-by-selection) 0)]
				                  (if (not prev-selected?)
					            (game-utils-aiamg/update-scene-from-cell-coords board camera [previous-cell-coord-in-focus]
						                                                    @selected-cell-indexes
						  				                    [{:row-index (:row-index previous-cell-coord-in-focus)
												      :column-index (:column-index previous-cell-coord-in-focus)}]
												     {:top Color/blue :bottom Color/blue
												      :left Color/blue :right Color/blue})
					            (game-utils-aiamg/update-scene-from-cell-coords board camera [previous-cell-coord-in-focus]
						                                                    @selected-cell-indexes
												    nil
												    nil)
					          )
					     )
					   )
				           (game-utils-aiamg/update-scene-from-cell-coords board camera [cell-coord-in-focus]
					                                                   @selected-cell-indexes @mouse-over-cell-indexes
								 	                   @mouse-over-cell-frame-color)
				           (.showScene camera)
				           (finally (.unlock sync-lock))
					 )
				       )
				     )
                                     (recur cell-coord-in-focus
				            (.getCurrentMouseEventOnScreen camera)
				            (.getCurrentMouseMovedEventOnScreen camera)
				     )
				   )
			         )
			       )
			  )

			  (do
			    (if (not= nil previous-cell-coord-in-focus)
			      (let [
				     prev-i (:row-index previous-cell-coord-in-focus)
				     prev-j (:column-index previous-cell-coord-in-focus)
				     prev-filtered-by-selection (filter #(and (= (:row-index %) prev-i) (= (:column-index %) prev-j)) @selected-cell-indexes)
				     prev-selected? (> (count prev-filtered-by-selection) 0)
				   ]
				   (if (not @interrupt)
			             (do
				       (.lock sync-lock)
				       (try
				         (reset! mouse-over-cell-indexes nil)
		                         (reset! mouse-over-cell-frame-color nil)
				         (if (not prev-selected?)
			                   (game-utils-aiamg/update-scene-from-cell-coords board camera [previous-cell-coord-in-focus] @selected-cell-indexes [{:row-index prev-i :column-index prev-j}] {:top Color/blue :bottom Color/blue :left Color/blue :right Color/blue})
			                   (game-utils-aiamg/update-scene-from-cell-coords board camera [previous-cell-coord-in-focus] @selected-cell-indexes [{:row-index prev-i :column-index prev-j}] {:top Color/darkGray :bottom Color/darkGray :left Color/darkGray :right Color/darkGray})
				         )
			                 (.showScene camera)
				         (finally (.unlock sync-lock))
				       )
				     )
				   )
			      )
			    )
			    (recur nil
			           (.getCurrentMouseEventOnScreen camera)
			           (.getCurrentMouseMovedEventOnScreen camera)
			    )
			  )
			)
		   )
		   (recur previous-cell-coord-in-focus
		          (.getCurrentMouseEventOnScreen camera)
		          (.getCurrentMouseMovedEventOnScreen camera)
		   )
		 )
	       )
	     )
       )
  )
)

