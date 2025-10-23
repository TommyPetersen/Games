(ns games.connect-four.interaktiv-grafikbaseret-spiller
  (:require [clojure.test :refer :all]
	    (games [game-utilities-aiamg :as game-utils-aiamg]
	           [game-utilities-misc :as game-utils-misc])
            (games.connect-four [connect-four-utilities-aiamg :as connect-four-utils-aiamg]
                                [connect-four-utilities-misc :as connect-four-utils-misc])
    	    [clojure.string :as str]
  )
  (:import [java.util.concurrent.locks ReentrantLock])
)

    ;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;                   ;;;
    ;;; * Connect four  * ;;;
    ;;;                   ;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;


(defn new-player [player-number]
  (let [
         camera (atom nil)
	 window-width 800
	 window-height 600
	 base-frame (game-utils-aiamg/calculate-base-frame window-width window-height)
         cell-grid-coords (game-utils-aiamg/generate-cell-grid-coords 7 6 base-frame)
         border-coords (:border-coords cell-grid-coords)
         cell-coords (:cell-coords cell-grid-coords)
	 board (atom (connect-four-utils-misc/empty-board 7))
	 time-unit 1000
	 time-limit 120000
	 selected-cell-index (atom nil)
	 mouse-over-cell-index (atom nil)
	 mouse-over-cell-frame-color (atom nil)
	 countdown-frame-left (game-utils-aiamg/calculate-aux-frame (:base-frame-left-border base-frame) (:left border-coords) (:top border-coords) (:bottom border-coords) 50 10 85 0)
 	 countdown-frame-right (game-utils-aiamg/calculate-aux-frame (:right border-coords) (:base-frame-right-border base-frame) (:top border-coords) (:bottom border-coords) 10 50 85 0)
	 countdown-frame-player (if (= player-number 1) countdown-frame-left countdown-frame-right)
	 countdown-frame-opponent  (if (= player-number 1) countdown-frame-right countdown-frame-left)
	 player-chip (if (= player-number 1) "*" "¤")
	 opponent-chip (if (= player-number 1) "¤" "*")
	 continue-going-opponent (atom (atom false))
	 sync-lock-graphics (ReentrantLock. )
	 interrupt-get-move (atom false)
	 show-game-score (fn [total-time-used-player total-time-used-opponent time-limit]
	                   (game-utils-aiamg/gui-show-board @board @camera base-frame border-coords cell-coords [@selected-cell-index])
			   (game-utils-aiamg/update-scene-from-cell-coords @board @camera cell-coords [@selected-cell-index] @mouse-over-cell-index @mouse-over-cell-frame-color)
    			   (game-utils-aiamg/show-aux-frame @camera countdown-frame-player)
			   (game-utils-aiamg/show-countdown @camera player-chip total-time-used-player time-limit (+ (:frame-x0 countdown-frame-player) 1) (+ (:frame-y0 countdown-frame-player) 1) (- (:frame-x1 countdown-frame-player) 1) (- (:frame-y1 countdown-frame-player) 1))
    			   (game-utils-aiamg/show-aux-frame @camera countdown-frame-opponent)
			   (game-utils-aiamg/show-countdown @camera opponent-chip total-time-used-opponent time-limit (+ (:frame-x0 countdown-frame-opponent) 1) (+ (:frame-y0 countdown-frame-opponent) 1) (- (:frame-x1 countdown-frame-opponent) 1) (- (:frame-y1 countdown-frame-opponent) 1))
	                 )
         get-user-move (fn [player-number]
			   (let [
				  go-loop-result-player (game-utils-misc/go-loop-on-atom
				                          (fn [v] (do
				                                    (.lock sync-lock-graphics)
				                                    (try
				                                      (.clearRaster @camera)
				                                      (show-game-score v 0 time-limit)
				    	                              (.showScene @camera)
				    	                              (finally
				                                        (.unlock sync-lock-graphics)
				                                      )
				                                    )
				                                  )
				                          )
				                          time-unit time-limit interrupt-get-move)
			          continue-going (:continue-going go-loop-result-player)
			  	  j (connect-four-utils-aiamg/get-user-move @board @camera window-width window-height border-coords cell-coords sync-lock-graphics selected-cell-index mouse-over-cell-index mouse-over-cell-frame-color interrupt-get-move)
			          _ (reset! continue-going false)
			        ]
			        (if (connect-four-utils-misc/column-valid? @board 7 6 j)
				  (do
				    (reset! selected-cell-index {:row-index 5 :column-index j})
				    (swap! board connect-four-utils-misc/insert j player-chip)
				    (if (connect-four-utils-misc/is-not-full? @board 6)
				      (let [
				             go-loop-result-opponent (game-utils-misc/go-loop-on-atom
				                                       (fn [v] (do
				                                                 (.lock sync-lock-graphics)
				                                                 (try
				                                                   (.clearRaster @camera)
				                                                   (show-game-score 0 v time-limit)
				    	                                           (.showScene @camera)
				    	                                           (finally
				                                                     (.unlock sync-lock-graphics)
				                                                   )
				                                                 )
				                                               )
				                                       )
				                                       time-unit time-limit interrupt-get-move)
				           ]
				           (reset! continue-going-opponent (:continue-going go-loop-result-opponent))
				      )
				      (do
				        (.lock sync-lock-graphics)
				        (try
				          (.clearRaster @camera)
				          (show-game-score 0 0 time-limit)
				    	  (.showScene @camera)
				    	  (finally
				            (.unlock sync-lock-graphics)
				          )
				        )
				      )
				    )
				  )
				)
				j
			   )
		       )
	 update-board (fn [unit-input]
	                  (let [
		                 move-string (nth (:data unit-input) 1)
				 j (Integer/parseInt move-string)
			       ]
			       (reset! @continue-going-opponent false)
			       (if (connect-four-utils-misc/column-valid? @board 7 6 j)
			         (do
				   (swap! board connect-four-utils-misc/insert j opponent-chip)
				 )
			       )
			       (.lock sync-lock-graphics)
			       (try
			         (.clearRaster @camera)
				 (reset! selected-cell-index {:row-index 5 :column-index j})
				 (show-game-score 0 0 time-limit)
				 (.showScene @camera)
				 (finally
				   (.unlock sync-lock-graphics)
				 )
			       )
			  )
		      )
       ]
       (fn [unit-input]
         (let [first-data-element (first (:data unit-input))]
             (case first-data-element
	        "initialiserSpil"	  (do
		                            (reset! camera (game-utils-aiamg/new-camera window-width window-height))
		                            (reset! board (connect-four-utils-misc/empty-board 7))
			                    (.lock sync-lock-graphics)
			                    (try
			                      (.clearRaster @camera)
				              (reset! selected-cell-index nil)
				              (show-game-score 0 0 time-limit)
				              (.showScene @camera)
				              (finally
				                (.unlock sync-lock-graphics)
				              )
			                    )
					    {:data ["Ok"]}
					  )
                "hentFoersteTraek"          {:data [(str (get-user-move player-number))]}
                "hentNaesteTraek"         (do
		                            (update-board unit-input)
		                            {:data [(str (get-user-move player-number))]}
					  )
                "meddelTraek"             (do
		                            (update-board unit-input)
					    {:data ["Accepteret"]}
					  )
					  
		{:data ["Fejl i data"]}
             )
        )
      )
  )
)


(def spiller1 (new-player 1))
(def spiller2 (new-player 2))

