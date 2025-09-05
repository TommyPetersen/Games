(ns games.infection.infection-utilities-aiamg
  (:require (games [game-utilities-aiamg :as game-utils-aiamg])
            (games.infection [infection-utilities-misc :as infection-utils-misc]))
  (:import (java.awt Color))
  (:import (java.awt.event MouseEvent))
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
      	                  board		; vec[kolonne-indeks 0...6][raekke-indeks 0...6]
	                  camera	; Aiamg.Camera
	                  window-width	; Klient-skaermens bredde i skaermpunkter, heltal stoerre end 0
	                  window-height	; Klient-skaermens hoejde i skaermpunkter, heltal stoerre end 0
	                  base-frame	; Til at tegne en kant udenom spilomraadet i klient-skaermen
	                  border-coords	; Spilomraadets graenser i klient-skaermen
	                  cell-coords	; Braettets celler udtrykt ved skaermkoordinater
	                  player-chip	; Spillerens brik udtrykt som tegnsymbol			   
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
				       (game-utils-aiamg/gui-show-board board camera base-frame cell-coords [{:row-index (:row-index previous-cell-coord-in-focus) :column-index (:column-index previous-cell-coord-in-focus)}])
				       (recur {:row-index (:row-index previous-cell-coord-in-focus) :column-index (:column-index previous-cell-coord-in-focus)}
				              nil
	                                      (.getCurrentMouseEventOnScreen camera)
			                      (.getCurrentMouseMovedEventOnScreen camera)
                                       )
				     )
				     (do
				       (game-utils-aiamg/gui-show-board board camera base-frame cell-coords [previous-selected-cell-index {:row-index (:row-index previous-cell-coord-in-focus) :column-index (:column-index previous-cell-coord-in-focus)}])
				       {:from-cell previous-selected-cell-index :to-cell {:row-index (:row-index previous-cell-coord-in-focus) :column-index (:column-index previous-cell-coord-in-focus)}}
				     )
				   )
				 )
				 (if (= (.getButton mouse-event) MouseEvent/BUTTON3)
				   (do
				     (game-utils-aiamg/gui-show-board board camera base-frame cell-coords [])
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
		                        transformed-coords (game-utils-aiamg/transform-coords-in-mouse-event insets mouse-moved-event window-width window-height)
			                transformed-x (:transformed-x transformed-coords)
			                transformed-y (:transformed-y transformed-coords)
		                      ]
		                      (if (and (>= transformed-x (:left border-coords)) (<= transformed-x (:right border-coords))
		                               (>= transformed-y (:bottom border-coords)) (<= transformed-y (:top border-coords))
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
						     (game-utils-aiamg/update-scene-from-cell-coords board camera [previous-cell-coord-in-focus] [previous-selected-cell-index] [cell-index-in-focus] {:top Color/blue :bottom Color/blue :left Color/blue :right Color/blue})
						     (.showScene camera)
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
					           (if (not= nil previous-cell-coord-in-focus) (game-utils-aiamg/update-scene-from-cell-coords board camera [previous-cell-coord-in-focus] [previous-selected-cell-index] [cell-index-in-focus] {:top Color/blue :bottom Color/blue :left Color/blue :right Color/blue}))
						   (game-utils-aiamg/update-scene-from-cell-coords board camera [cell-coord-in-focus] [previous-selected-cell-index] [cell-index-in-focus] {:top Color/gray :bottom Color/gray :left Color/gray :right Color/gray})
						   (.showScene camera)
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
					      (game-utils-aiamg/update-scene-from-cell-coords board camera [previous-cell-coord-in-focus] [previous-selected-cell-index] [{:row-index (:row-index previous-cell-coord-in-focus) :column-index (:column-index previous-cell-coord-in-focus)}] {:top Color/blue :bottom Color/blue :left Color/blue :right Color/blue})
					      (.showScene camera)
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
		    ]
  (let [
         selected-move (get-user-selection board camera window-width window-height base-frame border-coords cell-coords player-chip)
       ]
       {
         :from-coord [(:column-index (:from-cell selected-move)) (:row-index (:from-cell selected-move))]
         :to-coord [(:column-index (:to-cell selected-move)) (:row-index (:to-cell selected-move))]
       }
  )
)
