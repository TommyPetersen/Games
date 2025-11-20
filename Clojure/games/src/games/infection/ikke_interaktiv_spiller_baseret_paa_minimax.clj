(ns games.infection.ikke-interaktiv-spiller-baseret-paa-minimax
  (:require [clojure.test :refer :all]
	    (games.infection [infektion-hjaelpefunktioner-diverse :as infektion-hjlp-div])
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
			  (infektion-hjlp-div/init-board player-chip opponent-chip)
			  (infektion-hjlp-div/init-board opponent-chip player-chip)
			)
	            )
         board (atom (init-board))
         get-move (fn []
		      (let [
		             max-ply-depth 3
			     move (infektion-hjlp-div/next-move @board max-ply-depth)
			   ]
			   (if (infektion-hjlp-div/move-valid? @board player-chip move)
			     (swap! board infektion-hjlp-div/make-move move)
			   )
			   (str move)
		      )
		  )
	 update-board (fn [unit-input]
	                (let [
		               move-string (nth (:data unit-input) 1)
			       move (edn/read-string move-string)
			     ]
			     (if (infektion-hjlp-div/move-valid? @board opponent-chip move)
			       (do
			         (swap! board infektion-hjlp-div/make-move move)
			       )
			     )
			)
		      )
       ]
       (fn [unit-input]
         (let [first-data-element (first (:data unit-input))]
              (case first-data-element
	        "initialiserSpil"   (do
		                      (reset! board (init-board))
			              {:data ["Ok"]}
			            )
                "hentFoersteTraek"  {:data [(str (get-move))]}
                "hentNaesteTraek"   (do
		                      (update-board unit-input)
		                      {:data [(str (get-move))]}
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

