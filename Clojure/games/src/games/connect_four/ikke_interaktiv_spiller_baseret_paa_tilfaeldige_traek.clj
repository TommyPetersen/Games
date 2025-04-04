(ns games.connect-four.ikke-interaktiv-spiller-baseret-paa-tilfaeldige-traek
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
	     "initialiserSpil"  (do
		                  (new-board)
			          {:data ["Ok"]}
			        )
             "hentFoersteTraek" {:data [(str (get-move player-number))]}
             "hentNaesteTraek"  (do
		                  (update-board unit-input)
		                  {:data [(str (get-move player-number))]}
			        )
             "meddelTraek"      (do
		                  (update-board unit-input)
			          {:data ["Accepteret"]}
			        )

	     {:data ["Fejl i data"]}
           )
      )
    )
  )
)

(def spiller1 (new-player 1))
(def spiller2 (new-player 2))

