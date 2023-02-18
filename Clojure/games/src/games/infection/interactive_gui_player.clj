(ns games.infection.interactive-gui-player
  (:require [clojure.test :refer :all]
            (games [game-utilities :as game-utils])
	    (games.infection [infection-utilities :as infection-utils])
    	    [clojure.string :as str]
	    [clojure.edn :as edn]
  )
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
	 window-height 800
	 base-frame (game-utils/calculate-base-frame window-width window-height)
         cell-grid-coords (game-utils/generate-cell-grid-coords 7 7 base-frame)
         border-coords (:border-coords cell-grid-coords)
         cell-coords (:cell-coords cell-grid-coords)
         board (atom (infection-utils/init-board "*" "¤"))
	 player-chip (if (= player-number 1) "*" "¤")
	 opponent-chip (if (= player-number 1) "¤" "*")
         get-user-move (fn [player-number]
			   (if (infection-utils/cannot-move? @board player-chip)
			     (do
			       (println (str "\tNo possible move on board...auto passing"))
			       (str {:from-coord [-1 -1] :to-coord [-1 -1]})
			     )
			     (let [move (infection-utils/get-user-move @board @camera window-width window-height base-frame border-coords cell-coords)]
			          (if (infection-utils/move-valid? @board player-chip move)
				    (do
				      (swap! board infection-utils/make-move move)
    				      (game-utils/gui-show-board @board @camera base-frame cell-coords nil)
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
			       (if (infection-utils/move-valid? @board opponent-chip move)
			         (swap! board infection-utils/make-move move)
			       )
			       (game-utils/gui-show-board @board @camera base-frame cell-coords [from-cell to-cell])
			  )
		      )
       ]
       (fn [unit-input]
         (let [first-data-element (first (:data unit-input))]
             (case first-data-element
	        "initGame"		  (do
		                            (reset! camera (game-utils/new-camera window-width window-height))
		                            (reset! board (infection-utils/init-board "*" "¤"))
					    (game-utils/gui-show-board @board @camera base-frame cell-coords nil)
					    {:data ["Ok"]}
					  )
                "getFirstMove"            {:data [(str (get-user-move player-number))]}
                "getNextMove"             (do
		                            (update-board unit-input)
					    {:data [(str (get-user-move player-number))]}
					  )
                "notifyMove"              (do
		                            (update-board unit-input)
					    {:data ["Accepted"]}
					  )
             )
        )
      )
  )
)


(def player1 (new-player 1))
(def player2 (new-player 2))


;;; TESTS ;;;

(deftest unit-test
  (testing "Unit"
    (let [a 1]
      (is (= a 1))
    )
  )
)
