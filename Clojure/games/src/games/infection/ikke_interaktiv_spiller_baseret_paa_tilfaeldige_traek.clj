(ns games.infection.ikke-interaktiv-spiller-baseret-paa-tilfaeldige-traek
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
	        "initialiserSpil"	  (do
		                            (reset! board (infection-utils-misc/init-board "*" "¤"))
					    {:data ["Ok"]}
					  )
                "hentFoersteTraek"          {:data [(str (get-user-move player-number))]}
                "hentNaesteTraek"           (do
		                            (update-board unit-input)
		                            {:data [(str (get-user-move player-number))]}
					  )
                "meddelTraek"             (do
		                            (update-board unit-input)
					    {:data ["Accepteret"]}
					  )
             )
        )
      )
  )
)


(def spiller1 (new-player 1))
(def spiller2 (new-player 2))

