(ns games.connect-four.connect-four-utilities-aiamg
  (:require (games [game-utilities-aiamg :as game-utils-aiamg]))
  (:require (games.connect-four [connect-four-utilities-misc :as connect-four-utils-misc]))
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

(defn get-user-move [board camera window-width window-height border-coords cell-coords]
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
	     (if (and (not= nil mouse-event) (= (.getButton mouse-event) MouseEvent/BUTTON1))
	         (if (= nil previous-cell-coord-in-focus)
                     (recur previous-cell-coord-in-focus
	                    (.getCurrentMouseEventOnScreen camera)
			    (.getCurrentMouseMovedEventOnScreen camera)
		     )
	             (:column-index previous-cell-coord-in-focus)
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
				                                      (let [
				                                             selection-pred #(= (:column-index cell-coord-in-focus) %)
								           ]
								           (boolean (some selection-pred valid-column-seq))
								      )
								 )
			         ]
				 (if (not cell-coord-in-focus-is-valid?)
				   (do
				     (if (not= nil previous-cell-coord-in-focus)
				       (do
				         (game-utils-aiamg/update-scene-from-cell-coords board camera [previous-cell-coord-in-focus] [] [cell-index-in-focus] {:top Color/blue :bottom Color/blue :left Color/blue :right Color/blue})
				         (.showScene camera)
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
				       (if (not= nil previous-cell-coord-in-focus) (game-utils-aiamg/update-scene-from-cell-coords board camera [previous-cell-coord-in-focus] [] [cell-index-in-focus] {:top Color/blue :bottom Color/blue :left Color/blue :right Color/blue}))
				       (game-utils-aiamg/update-scene-from-cell-coords board camera [cell-coord-in-focus] [] [cell-index-in-focus] {:top Color/blue :bottom Color/blue :left Color/gray :right Color/gray})
				       (.showScene camera)
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
			        (do
			          (game-utils-aiamg/update-scene-from-cell-coords board camera [previous-cell-coord-in-focus] [] [{:row-index (:row-index previous-cell-coord-in-focus) :column-index (:column-index previous-cell-coord-in-focus)}] {:top Color/blue :bottom Color/blue :left Color/blue :right Color/blue})
			          (.showScene camera)
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
