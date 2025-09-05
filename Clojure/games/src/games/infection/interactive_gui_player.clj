(ns games.infection.interactive-gui-player
  (:require [clojure.test :refer :all]
            (games [game-utilities-aiamg :as game-utils-aiamg])
	    (games.infection [infection-utilities-aiamg :as infection-utils-aiamg]
	                     [infection-utilities-misc :as infection-utils-misc])
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
	 base-frame (game-utils-aiamg/calculate-base-frame window-width window-height)
         cell-grid-coords (game-utils-aiamg/generate-cell-grid-coords 7 7 base-frame)
         border-coords (:border-coords cell-grid-coords)
         cell-coords (:cell-coords cell-grid-coords)
         board (atom (infection-utils-misc/init-board "*" "¤"))
	 player-chip (if (= player-number 1) "*" "¤")
	 opponent-chip (if (= player-number 1) "¤" "*")
         get-user-move (fn [player-number]
			   (if (infection-utils-misc/cannot-move? @board player-chip)
			     (do
			       (println (str "\tNo possible move on board...auto passing"))
			       (str {:from-coord [-1 -1] :to-coord [-1 -1]})
			     )
			     (let [move (infection-utils-aiamg/get-user-move @board @camera window-width window-height base-frame border-coords cell-coords player-chip)]
			          (if (infection-utils-misc/move-valid? @board player-chip move)
				    (do
				      (swap! board infection-utils-misc/make-move move)
    				      (game-utils-aiamg/gui-show-board @board @camera base-frame cell-coords nil)
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
			         (swap! board infection-utils-misc/make-move move)
			       )
			       (game-utils-aiamg/gui-show-board @board @camera base-frame cell-coords [from-cell to-cell])
			  )
		      )
       ]
       (fn [unit-input]
         (let [first-data-element (first (:data unit-input))]
             (case first-data-element
	        "init-game"		  (do
		                            (reset! camera (game-utils-aiamg/new-camera window-width window-height))
		                            (reset! board (infection-utils-misc/init-board "*" "¤"))
					    (game-utils-aiamg/gui-show-board @board @camera base-frame cell-coords nil)
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
