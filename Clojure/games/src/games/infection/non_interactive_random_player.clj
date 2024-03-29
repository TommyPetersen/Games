(ns games.infection.non-interactive-random-player
  (:require [clojure.test :refer :all]
	    (games.infection [infection-utilities-misc :as infection-utils-misc])
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
         board (atom (infection-utils-misc/init-board "*" "¤"))
	 player-chip (if (= player-number 1) "*" "¤")
	 opponent-chip (if (= player-number 1) "¤" "*")
         get-user-move (fn [player-number]
			   (let [
				  move (infection-utils-misc/get-random-valid-move @board player-chip)
			        ]
			        (if (infection-utils-misc/move-valid? @board player-chip move)
				  (swap! board infection-utils-misc/make-move move)
				)
				(str move)
			   )
		       )
	 update-board (fn [unit-input]
	                  (let [
		                 move-string (nth (:data unit-input) 1)
				 move (edn/read-string move-string)
			       ]
			       (if (infection-utils-misc/move-valid? @board opponent-chip move)
			         (swap! board infection-utils-misc/make-move move)
			       )
			  )
		      )
       ]
       (fn [unit-input]
         (let [first-data-element (first (:data unit-input))]
             (case first-data-element
	        "init-game"		  (do
		                            (reset! board (infection-utils-misc/init-board "*" "¤"))
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
