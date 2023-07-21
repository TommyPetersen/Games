(ns games.infection.non-interactive-minimax-player
  (:require [clojure.test :refer :all]
            (dk-aia-clojure [definitions :as aia-defs])
	    (games.infection [infection-utilities-misc :as infection-utils-misc])
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
			  (infection-utils-misc/init-board player-chip opponent-chip)
			  (infection-utils-misc/init-board opponent-chip player-chip)
			)
	            )
         board (atom (init-board))
         get-move (fn []
		      (let [
		             max-ply-depth 3
			     move (infection-utils-misc/next-move @board max-ply-depth)
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
	        "init-game"         (do
		                      (reset! board (init-board))
			              {:data ["Ok"]}
			            )
                "get-first-move"    {:data [(str (get-move))]}
                "get-next-move"     (do
		                      (update-board unit-input)
		                      {:data [(str (get-move))]}
			            )
                "notify-move"       (do
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
