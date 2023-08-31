(ns games.connect-four.non-interactive-random-player
  (:require [clojure.test :refer :all]
            (dk-aia-clojure [definitions :as aia-defs])
            (games.connect-four [connect-four-utilities-misc :as connect-four-utils-misc])
    	    [clojure.string :as str]
  )
)


    ;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;                   ;;;
    ;;; * Connect four *  ;;;
    ;;;                   ;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;


(defn new-board-fn []
  #(let [_ %] (connect-four-utils-misc/empty-board 7))
)

(defn update-board-fn [column-number chip]
  #(connect-four-utils-misc/insert % column-number chip)
)

(defn new-player [player-number]
  (let [
         board (atom (connect-four-utils-misc/empty-board 7))
	 player-chip (if (= player-number 1) "1" "2")
	 opponent-chip (if (= player-number 1) "2" "1")
         get-move (fn [player-number]
		      (let [j (connect-four-utils-misc/get-random-valid-column @board 6)]
			   (if (connect-four-utils-misc/column-valid? @board 7 6 j)
			     (swap! board connect-four-utils-misc/insert j player-chip)
			   )
			   (str j)
		      )
		  )
	 update-board (fn [unit-input]
	                (let [
		               move-string (nth (:data unit-input) 1)
			       j (Integer/parseInt move-string)
			     ]
			     (if (connect-four-utils-misc/column-valid? @board 7 6 j)
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
	     "init-game"      (do
		                (new-board)
			        {:data ["Ok"]}
			      )
             "get-first-move" {:data [(str (get-move player-number))]}
             "get-next-move"  (do
		                (update-board unit-input)
		                {:data [(str (get-move player-number))]}
			      )
             "notify-move"   (do
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
