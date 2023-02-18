(ns games.infection.non-interactive-random-player
  (:require [clojure.test :refer :all]
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
         board (atom (infection-utils/init-board "*" "¤"))
	 player-chip (if (= player-number 1) "*" "¤")
	 opponent-chip (if (= player-number 1) "¤" "*")
         get-user-move (fn [player-number]
	                   (println (str "\n\tYour board player " player-number ":"))
	                   (println (str "\n" (infection-utils/board-to-str @board "\t\t") "\n"))
	                   (println (str "\tPlayer" player-number ", enter your move: "))
			   (flush)
			   (let [
				  move (infection-utils/get-random-valid-move @board player-chip)
			        ]
			        (if (infection-utils/move-valid? @board player-chip move)
				  (swap! board infection-utils/make-move move)
				)
				(println (str "\n\tYour updated board player " player-number ":"))
	                        (println (str "\n" (infection-utils/board-to-str @board "\t\t") "\n"))
				(str move)
			   )
		       )
	 update-board (fn [unit-input]
	                  (let [
		                 move-string (nth (:data unit-input) 1)
				 move (edn/read-string move-string)
			       ]
			       (if (infection-utils/move-valid? @board opponent-chip move)
			         (swap! board infection-utils/make-move move)
			       )
			  )
		      )
       ]
       (fn [unit-input]
         (let [first-data-element (first (:data unit-input))]
             (case first-data-element
	        "initGame"		  (do
		                            (reset! board (infection-utils/init-board "*" "¤"))
					    {:data ["Ok"]}
					  )
                "getFirstMove"            {:data [(str (get-user-move player-number))]}
                "getNextMove"             (do
		                            (update-board unit-input)
		                            {:data [(str (get-user-move player-number))]}
					  )
                "notifyMove"              (do
		                            (update-board unit-input)
					    (println (str "\n\tYour board player " player-number ":"))
					    (println (str "\n" (infection-utils/board-to-str @board "\t\t") "\n"))
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
