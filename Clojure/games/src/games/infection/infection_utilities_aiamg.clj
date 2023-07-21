(ns games.infection.infection-utilities-aiamg
  (:require (games [game-utilities-aiamg :as game-utils-aiamg]))
  (:import (java.awt.event MouseEvent))
)

(defn find-column-number [x cell-coords]
  (let [pred #(and (>= x (:cell-left-border %)) (< x (:cell-right-border %)))]
       (:column-index (first (filter pred cell-coords)))
  )
)

(defn find-cell-index [x y cell-coords]
  (let [
         pred #(and (>= x (:cell-left-border %)) (< x (:cell-right-border %))
                    (>= y (:cell-bottom-border %)) (< y (:cell-top-border %)))
	 cell-coord (first (filter pred cell-coords))
       ]
       {:row-index (:row-index cell-coord) :column-index (:column-index cell-coord)}
  )
)

(defn get-user-selection [camera window-width window-height border-coords cell-coords]
  (let [
         insets (.getInsetsOnScreen camera)
	 find-cell-fn #(loop [mouse-event (.getCurrentMouseEventOnScreen camera)]
	                    (if (and (not= nil mouse-event) (= (.getButton mouse-event) MouseEvent/BUTTON1))
	                        (let [
		                       transformed-coords (game-utils-aiamg/transform-coords-in-mouse-event insets mouse-event window-width window-height)
			               transformed-x (:transformed-x transformed-coords)
			               transformed-y (:transformed-y transformed-coords)
		                     ]
		                     (if (and (>= transformed-x (:left border-coords)) (<= transformed-x (:right border-coords))
		                              (>= transformed-y (:bottom border-coords)) (<= transformed-y (:top border-coords))
		                         )
		                         (find-cell-index transformed-x transformed-y cell-coords)
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
       ]
       (find-cell-fn)
  )
)

(defn get-user-move [board camera window-width window-height base-frame border-coords cell-coords]
  (let [
         from-cell (get-user-selection camera window-width window-height border-coords cell-coords)
	 _ (game-utils-aiamg/gui-show-board board camera base-frame cell-coords [from-cell])
	 to-cell (get-user-selection camera window-width window-height border-coords cell-coords)
	 _ (game-utils-aiamg/gui-show-board board camera base-frame cell-coords [from-cell to-cell])
       ]
       {:from-coord [(:column-index from-cell) (:row-index from-cell)] :to-coord [(:column-index to-cell) (:row-index to-cell)]}
  )
)
