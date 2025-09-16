(ns games.connect-four.interactive-gui-player
  (:require [clojure.test :refer :all]
	    (games [game-utilities-aiamg :as game-utils-aiamg])
            (games.connect-four [connect-four-utilities-aiamg :as connect-four-utils-aiamg]
                                [connect-four-utilities-misc :as connect-four-utils-misc])
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
	 base-frame (game-utils-aiamg/calculate-base-frame window-width window-height)
         cell-grid-coords (game-utils-aiamg/generate-cell-grid-coords 7 6 base-frame)
         border-coords (:border-coords cell-grid-coords)
         cell-coords (:cell-coords cell-grid-coords)
	 board (atom (connect-four-utils-misc/empty-board 7))
	 player-chip (if (= player-number 1) "*" "¤")
	 opponent-chip (if (= player-number 1) "¤" "*")
         get-user-move (fn [player-number]
			   (let [
			  	  j (connect-four-utils-aiamg/get-user-move @board @camera window-width window-height border-coords cell-coords)
			        ]
			        (if (connect-four-utils-misc/column-valid? @board 7 6 j)
				  (do
				    (swap! board connect-four-utils-misc/insert j player-chip)
    				    (game-utils-aiamg/gui-show-board @board @camera base-frame border-coords cell-coords [{:row-index 5 :column-index j}])
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
			       (if (connect-four-utils-misc/column-valid? @board 7 6 j)
			         (do
				   (swap! board connect-four-utils-misc/insert j opponent-chip)
				 )
			       )
    			       (game-utils-aiamg/gui-show-board @board @camera base-frame border-coords cell-coords [{:row-index 5 :column-index j}])
			  )
		      )
       ]
       (fn [unit-input]
         (let [first-data-element (first (:data unit-input))]
             (case first-data-element
	        "init-game"		  (do
		                            (reset! camera (game-utils-aiamg/new-camera window-width window-height))
		                            (reset! board (connect-four-utils-misc/empty-board 7))
					    (game-utils-aiamg/gui-show-board @board @camera base-frame border-coords cell-coords nil)
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
