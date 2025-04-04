(ns games.infection.ikke-interaktiv-spiller-baseret-paa-minimax
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
			       (do
			         (swap! board infection-utils-misc/make-move move)
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

