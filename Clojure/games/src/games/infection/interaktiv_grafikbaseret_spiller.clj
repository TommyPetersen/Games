(ns games.infection.interaktiv-grafikbaseret-spiller
  (:require [clojure.test :refer :all]
            (games [game-utilities-aiamg :as game-utils-aiamg]
	           [game-utilities-misc :as game-utils-misc])
	    (games.infection [infection-utilities-aiamg :as infection-utils-aiamg]
	                     [infection-utilities-misc :as infection-utils-misc])
    	    [clojure.string :as str]
	    [clojure.edn :as edn]
  )
  (:import [java.util.concurrent.locks ReentrantLock])
)


    ;;;;;;;;;;;;;;;;;;;;;;
    ;;;                ;;;
    ;;; * Infection  * ;;;
    ;;;                ;;;
    ;;;;;;;;;;;;;;;;;;;;;;


(defn new-player [player-number]
  (let [
         camera (atom nil)
	 window-width 800
	 window-height 600
	 base-frame (game-utils-aiamg/calculate-base-frame window-width window-height)
         cell-grid-coords (game-utils-aiamg/generate-cell-grid-coords 7 7 base-frame)
         border-coords (:border-coords cell-grid-coords)
         cell-coords (:cell-coords cell-grid-coords)
         board (atom (infection-utils-misc/init-board "*" "¤"))
	 boards (atom [@board])
	 history-length 15
	 stats-frame (game-utils-aiamg/calculate-aux-frame (:left border-coords) (+ (:left border-coords) (* 20 history-length)) (:base-frame-top-border base-frame) (:top border-coords) 0 0 10 10)
	 available-x-delta (- (:right border-coords) (:frame-x1 stats-frame))
	 countdown-frame-player (game-utils-aiamg/calculate-aux-frame (:frame-x1 stats-frame) (:right border-coords) (:base-frame-top-border base-frame) (:top border-coords) 3 93 10 10)
	 player-chip (if (= player-number 1) "*" "¤")
	 opponent-chip (if (= player-number 1) "¤" "*")
	 sync-lock (ReentrantLock. )
	 show-game-score (fn []
	                   (game-utils-aiamg/gui-show-board @board @camera base-frame border-coords cell-coords nil)
	 	           (game-utils-aiamg/show-aux-frame @camera stats-frame)
	 	           (let [
	 		          historic-boards (drop (- (count @boards) history-length) @boards)
	 		        ]
	 		        (game-utils-aiamg/show-graphs @camera (infection-utils-misc/count-symbols-in-boards historic-boards player-chip opponent-chip) history-length (+ 5 (:frame-x0 stats-frame)) (+ 1 (:frame-y0 stats-frame)) (- (:frame-x1 stats-frame) 5) (- (:frame-y1 stats-frame) 5))
	                   )
	                 )
         get-user-move (fn [player-number]
			   (if (infection-utils-misc/cannot-move? @board player-chip)
			     (do
			       (println (str "\tIngen mulige traek paa braettet...melder pas"))
			       (str {:from-coord [-1 -1] :to-coord [-1 -1]})
			     )
			     (let [
				    go-loop-result (game-utils-misc/go-loop-on-atom
				      #(do
				         (.lock sync-lock)
					 (try
				           (.clearRaster @camera)
					   (show-game-score)
    				           (game-utils-aiamg/show-aux-frame @camera countdown-frame-player)
				           (game-utils-aiamg/show-countdown @camera player-chip % 120000 (+ (:frame-x0 countdown-frame-player) 1) (+ (:frame-y0 countdown-frame-player) 1) (- (:frame-x1 countdown-frame-player) 1) (- (:frame-y1 countdown-frame-player) 1))
					   (.showScene @camera)
					   (finally
				             (.unlock sync-lock)
				           )
					 )
				       )
				       1000 120000)
			            continue-going (:continue-going go-loop-result)
			            move (infection-utils-aiamg/get-user-move @board @camera window-width window-height base-frame border-coords cell-coords player-chip sync-lock)
			            _ (reset! continue-going false)
				  ]
			          (if (infection-utils-misc/move-valid? @board player-chip move)
				    (do
				         (swap! board infection-utils-misc/make-move move)
				         (swap! boards conj @board)
					 (.lock sync-lock)
					 (try
				           (.clearRaster @camera)
 					   (show-game-score)
    				           (game-utils-aiamg/show-aux-frame @camera countdown-frame-player)
				           (game-utils-aiamg/show-countdown @camera player-chip (:total-time-used go-loop-result) 120000 (+ (:frame-x0 countdown-frame-player) 1) (+ (:frame-y0 countdown-frame-player) 1) (- (:frame-x1 countdown-frame-player) 1) (- (:frame-y1 countdown-frame-player) 1))
				           (.showScene @camera)
					   (finally
				             (.unlock sync-lock)
				           )
					 )
				    )
				  )
				  (str move)
			     )
			   )
		       )
	 update-board (fn [unit-input]
	                  (let [
		                 move-string (nth (:data unit-input) 1)
				 move (edn/read-string move-string)
				 from-cell {:row-index (second (:from-coord move)) :column-index (first (:from-coord move))}
				 to-cell {:row-index (second (:to-coord move)) :column-index (first (:to-coord move))}
			       ]
			       (if (infection-utils-misc/move-valid? @board opponent-chip move)
			         (do
			           (swap! board infection-utils-misc/make-move move)
				   (swap! boards conj @board)
				 )
			       )
			       (.lock sync-lock)
			       (try
			         (.clearRaster @camera)
			         (show-game-score)
    			         (game-utils-aiamg/show-aux-frame @camera countdown-frame-player)
			         (game-utils-aiamg/show-countdown @camera player-chip 0 120000 (+ (:frame-x0 countdown-frame-player) 1) (+ (:frame-y0 countdown-frame-player) 1) (- (:frame-x1 countdown-frame-player) 1) (- (:frame-y1 countdown-frame-player) 1))
     			         (.showScene @camera)
			         (finally
				   (.unlock sync-lock)
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
		                            (reset! board (infection-utils-misc/init-board "*" "¤"))
					    (reset! boards [@board])
					    (.lock sync-lock)
					    (try
					      (.clearRaster @camera)
					      (show-game-score)
    			                      (game-utils-aiamg/show-aux-frame @camera countdown-frame-player)
			                      (game-utils-aiamg/show-countdown @camera player-chip 0 120000 (+ (:frame-x0 countdown-frame-player) 1) (+ (:frame-y0 countdown-frame-player) 1) (- (:frame-x1 countdown-frame-player) 1) (- (:frame-y1 countdown-frame-player) 1))
					      (.showScene @camera)
					      (finally
				                (.unlock sync-lock)
				              )
				            )
					    {:data ["Ok"]}
					  )
                "hentFoersteTraek"        {:data [(str (get-user-move player-number))]}
                "hentNaesteTraek"         (do
		                            (update-board unit-input)
					    {:data [(str (get-user-move player-number))]}
					  )
                "meddelTraek"             (do
		                            (update-board unit-input)
					    {:data ["Accepteret"]}
					  )
             )
        )
      )
  )
)


(def spiller1 (new-player 1))
(def spiller2 (new-player 2))

