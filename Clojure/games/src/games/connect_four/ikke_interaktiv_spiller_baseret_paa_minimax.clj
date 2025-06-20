(ns games.connect-four.ikke-interaktiv-spiller-baseret-paa-minimax
  (:require [clojure.test :refer :all]
            (games.connect-four [connect-four-utilities-misc :as connect-four-utils-misc])
    	    [clojure.string :as str]
  )
)


    ;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;                   ;;;
    ;;; * Connect four *  ;;;
    ;;;                   ;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;


(defn new-player [player-number]
  (let [
         board (atom (connect-four-utils-misc/empty-board 7))
	 player-chip "M"
	 opponent-chip "O"
	 column-distribution [1 4 9 72 9 4 1]
	 max-ply-depth 5
         get-move (fn [player-number first-move?]
		      (let [j (connect-four-utils-misc/next-move @board max-ply-depth first-move? column-distribution)]
			   (if (connect-four-utils-misc/column-valid? @board 7 6 j)
			     (swap! board connect-four-utils-misc/insert j player-chip)
			   )
			   (str j)
		      )
		  )
	 update-board (fn [unit-input]
	                (let [j (Integer/parseInt (nth (:data unit-input) 1))]
			     (if (connect-four-utils-misc/column-valid? @board 7 6 j)
			       (swap! board connect-four-utils-misc/insert j opponent-chip)
			     )
			)
		      )
       ]
       (fn [unit-input]
         (let [first-data-element (first (:data unit-input))]
              (case first-data-element
	        "initialiserSpil"   (do
		                      (reset! board (connect-four-utils-misc/empty-board 7))
			              {:data ["Ok"]}
			            )
                "hentFoersteTraek"  {:data [(str (get-move player-number true))]}
                "hentNaesteTraek"   (do
		                      (update-board unit-input)
		                      {:data [(str (get-move player-number false))]}
			            )
                "meddelTraek"       (do
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

