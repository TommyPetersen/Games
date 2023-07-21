(ns games.connect-four.connect-four-utilities-aiamg
  (:require (games [game-utilities-aiamg :as game-utils-aiamg]))
  (:import (java.awt.event MouseEvent))
)

(defn find-column-number [x cell-coords]
  (let [pred #(and (>= x (:cell-left-border %)) (< x (:cell-right-border %)))]
       (:column-index (first (filter pred cell-coords)))
  )
)

(defn get-user-move [camera window-width window-height border-coords cell-coords]
  (let [insets (.getInsetsOnScreen camera)]
       (loop [mouse-event (.getCurrentMouseEventOnScreen camera)]
	     (if (and (not= nil mouse-event) (= (.getButton mouse-event) MouseEvent/BUTTON1))
	         (let [
		        transformed-coords (game-utils-aiamg/transform-coords-in-mouse-event insets mouse-event window-width window-height)
			transformed-x (:transformed-x transformed-coords)
			transformed-y (:transformed-y transformed-coords)
		      ]
		      (if (and (>= transformed-x (:left border-coords)) (<= transformed-x (:right border-coords))
		               (>= transformed-y (:bottom border-coords)) (<= transformed-y (:top border-coords))
		          )
			  (find-column-number transformed-x cell-coords)
			  (do
                            (Thread/sleep 200)
                            (recur (.getCurrentMouseEventOnScreen camera))
	                  )
		      )
		 )
		 (do
                   (Thread/sleep 200)
                   (recur (.getCurrentMouseEventOnScreen camera))
	         )
	     )
       )
  )
)
