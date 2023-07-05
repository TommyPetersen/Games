(ns games.connect-four.interactive-gui-player
  (:require [clojure.test :refer :all]
	    (games [game-utilities :as game-utils])
            (games.connect-four [connect-four-utilities :as connect-four-utils])
    	    [clojure.string :as str]
  )
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
	 window-height 800
	 base-frame (game-utils/calculate-base-frame window-width window-height)
         cell-grid-coords (game-utils/generate-cell-grid-coords 7 6 base-frame)
         border-coords (:border-coords cell-grid-coords)
         cell-coords (:cell-coords cell-grid-coords)
	 board (atom (connect-four-utils/empty-board 7))
	 player-chip (if (= player-number 1) "*" "¤")
	 opponent-chip (if (= player-number 1) "¤" "*")
         get-user-move (fn [player-number]
			   (let [
			  	  j (connect-four-utils/get-user-move @camera window-width window-height border-coords cell-coords)
			        ]
			        (if (connect-four-utils/column-valid? @board 7 6 j)
				  (do
				    (swap! board connect-four-utils/insert j player-chip)
    				    (game-utils/gui-show-board @board @camera base-frame cell-coords [{:row-index 5 :column-index j}])
				  )
				)
				(+ j 1)
			   )
		       )
	 update-board (fn [unit-input]
	                  (let [
		                 move-string (nth (:data unit-input) 1)
				 j (- (Integer/parseInt move-string) 1)
			       ]
			       (if (connect-four-utils/column-valid? @board 7 6 j)
			         (do
				   (swap! board connect-four-utils/insert j opponent-chip)
				 )
			       )
    			       (game-utils/gui-show-board @board @camera base-frame cell-coords [{:row-index 5 :column-index j}])
			  )
		      )
       ]
       (fn [unit-input]
         (let [first-data-element (first (:data unit-input))]
             (case first-data-element
	        "init-game"		  (do
		                            (reset! camera (game-utils/new-camera window-width window-height))
		                            (reset! board (connect-four-utils/empty-board 7))
					    (game-utils/gui-show-board @board @camera base-frame cell-coords nil)
					    {:data ["Ok"]}
					  )
                "get-first-move"          {:data [(str (get-user-move player-number))]}
                "get-next-move"           (do
		                            (update-board unit-input)
		                            {:data [(str (get-user-move player-number))]}
					  )
                "notify-move"             (do
		                            (update-board unit-input)
					    {:data ["Accepted"]}
					  )
					  
		{:data ["Error in data"]}
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
