(ns games.infection.infection-utilities-aiamg
  (:require (games [game-utilities-aiamg :as game-utils-aiamg]))
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

(defn get-user-selection [board camera window-width window-height border-coords cell-coords]
  (let [
         insets (.getInsetsOnScreen camera)
	 find-cell-fn #(loop [
	                       prev-cell-coord nil
			       curr-cell-coord nil
	                       mouse-event (.getCurrentMouseEventOnScreen camera)
			       mouse-moved-event (.getCurrentMouseMovedEventOnScreen camera)
			     ]
                            (if (and (not= nil mouse-event) (= (.getButton mouse-event) MouseEvent/BUTTON1))
			      (if (= nil curr-cell-coord)
                                (recur prev-cell-coord
			               curr-cell-coord
				       (.getCurrentMouseEventOnScreen camera)
				       (.getCurrentMouseMovedEventOnScreen camera)
				)
				{:row-index (:row-index curr-cell-coord) :column-index (:column-index curr-cell-coord)}
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
					          cell-coord (find-cell-coord transformed-x transformed-y cell-coords)
						  cell-index {:row-index (:row-index cell-coord) :column-index (:column-index cell-coord)}
						]
					        (if (= cell-coord curr-cell-coord)
                                                  (recur prev-cell-coord
					                 curr-cell-coord
					                 (.getCurrentMouseEventOnScreen camera)
					                 (.getCurrentMouseMovedEventOnScreen camera)
					          )
						  (do
						    (if (not= nil curr-cell-coord) (game-utils-aiamg/update-scene-from-cell-coords board camera [curr-cell-coord] [] cell-index))
						    (game-utils-aiamg/update-scene-from-cell-coords board camera [cell-coord] [] cell-index)
						    (.showScene camera)
                                                    (recur curr-cell-coord
						           cell-coord
					                   (.getCurrentMouseEventOnScreen camera)
					                   (.getCurrentMouseMovedEventOnScreen camera)
					            )
						  )
					        )
	                                   )
					   (do
                                             (recur prev-cell-coord
					            curr-cell-coord
					            (.getCurrentMouseEventOnScreen camera)
					            (.getCurrentMouseMovedEventOnScreen camera)
					     )
					   )
		                       )
		                   )
		                   (do
                                     (recur prev-cell-coord
					    curr-cell-coord
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

(defn get-user-move [board camera window-width window-height base-frame border-coords cell-coords]
  (let [
         from-cell (get-user-selection board camera window-width window-height border-coords cell-coords)
	 _ (game-utils-aiamg/gui-show-board board camera base-frame cell-coords [from-cell])
	 to-cell (get-user-selection board camera window-width window-height border-coords cell-coords)
	 _ (game-utils-aiamg/gui-show-board board camera base-frame cell-coords [from-cell to-cell])
       ]
       {:from-coord [(:column-index from-cell) (:row-index from-cell)] :to-coord [(:column-index to-cell) (:row-index to-cell)]}
  )
)
