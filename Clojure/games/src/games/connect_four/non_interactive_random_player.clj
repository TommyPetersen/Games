(ns games.connect-four.non-interactive-random-player
  (:require [clojure.test :refer :all]
            (dk-aia-clojure [definitions :as aia-defs])
            (games.connect-four [connect-four-utilities :as connect-four-utils])
    	    [clojure.string :as str]
  )
)


    ;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;                   ;;;
    ;;; * Connect four *  ;;;
    ;;;                   ;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;


(defn new-board-fn []
  #(let [_ %] (connect-four-utils/empty-board 7))
)

(defn update-board-fn [column-number chip]
  #(connect-four-utils/insert % column-number chip)
)

(defn new-player [player-number]
  (let [
         board (atom (connect-four-utils/empty-board 7))
	 player-chip (if (= player-number 1) "1" "2")
	 opponent-chip (if (= player-number 1) "2" "1")
         get-move (fn [player-number]
	              (println (str "\n\tYour board player " player-number ":"))
	              (println (str "\n" (connect-four-utils/board-to-str @board 6 "\t\t") "\n"))
	              (print (str "\tPlayer" player-number ", enter your move (1 - 7): "))
		      (flush)
		      (let [
			     move-string (str (+ 1 (connect-four-utils/get-random-valid-column @board 6)))
			     j (- (Integer/parseInt move-string) 1)
			   ]
			   (if (connect-four-utils/column-valid? @board 7 6 j)
			     (swap! board connect-four-utils/insert j player-chip)
			   )
			   (println (str "\n\tYour updated board player " player-number ":"))
	                   (println (str "\n" (connect-four-utils/board-to-str @board 6 "\t\t") "\n"))
			   move-string
		      )
		  )
	 update-board (fn [unit-input]
	                (let [
		               move-string (nth (:data unit-input) 1)
			       j (- (Integer/parseInt move-string) 1)
			     ]
			     (if (connect-four-utils/column-valid? @board 7 6 j)
			       (swap! board (update-board-fn j opponent-chip))
			     )
			)
		      )
	 new-board (fn []
	             (swap! board (new-board-fn))
	           )
       ]
    (fn [unit-input]
      (let [first-data-element (first (:data unit-input))]
           (case first-data-element
	     "initGame"	    (do
		              (new-board)
			      {:data ["Ok"]}
			    )
             "getFirstMove" {:data [(str (get-move player-number))]}
             "getNextMove"  (do
		              (update-board unit-input)
		              {:data [(str (get-move player-number))]}
			    )
             "notifyMove"   (do
		              (update-board unit-input)
			      (println (str "\n\tYour board player " player-number ":"))
			      (println (str "\n" (connect-four-utils/board-to-str @board 6 "\t\t") "\n"))
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
