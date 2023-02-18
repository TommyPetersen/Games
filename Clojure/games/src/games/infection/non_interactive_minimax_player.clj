(ns games.infection.non-interactive-minimax-player
  (:require [clojure.test :refer :all]
            (dk-aia-clojure [definitions :as aia-defs])
	    (games.infection [infection-utilities :as infection-utils])
    	    [clojure.string :as str]
	    [clojure.edn :as edn]
  )
)


    ;;;;;;;;;;;;;;;;;;;;;;
    ;;;                ;;;
    ;;; * Infection *  ;;;
    ;;;                ;;;
    ;;;;;;;;;;;;;;;;;;;;;;


(defn new-player [player-number]
  (let [
	 player-chip "M"
	 opponent-chip "O"
         init-board (fn []
	                (if (= player-number 1)
			  (infection-utils/init-board player-chip opponent-chip)
			  (infection-utils/init-board opponent-chip player-chip)
			)
	            )
         board (atom (init-board))
         get-move (fn []
		      (let [
		             max-ply-depth 3
			     move (infection-utils/next-move @board max-ply-depth)
			   ]
			   (if (infection-utils/move-valid? @board player-chip move)
			     (swap! board infection-utils/make-move move)
			   )
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
	        "initGame"	    (do
		                      (reset! board (init-board))
			              {:data ["Ok"]}
			            )
                "getFirstMove" {:data [(str (get-move))]}
                "getNextMove"  (do
		                 (update-board unit-input)
		                 {:data [(str (get-move))]}
			       )
                "notifyMove"  (do
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
