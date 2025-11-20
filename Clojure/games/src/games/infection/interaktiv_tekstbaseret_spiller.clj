(ns games.infection.interaktiv-tekstbaseret-spiller
  (:require [clojure.test :refer :all]
	    (games.infection [infektion-hjaelpefunktioner-diverse :as infektion-hjlp-div])
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
         board (atom (infektion-hjlp-div/init-board "*" "¤"))
	 player-chip (if (= player-number 1) "*" "¤")
	 opponent-chip (if (= player-number 1) "¤" "*")
         get-user-move (fn [player-number]
	                   (println (str "\n\tDit braet spiller " player-number ":"))
	                   (println (str "\n" (infektion-hjlp-div/board-to-str @board "\t\t") "\n"))
	                   (println (str "\tSpiller " player-number ", indtast dit traek: "))
			   (flush)
			   (let [
			          move-string-j0 (do (print "\tTraek j0: ") (flush) (read-line))
				  j0 (- (Integer/parseInt move-string-j0) 1)
			          move-string-i0 (do (print "\tTraek i0: ") (flush) (read-line))
				  i0 (- (Integer/parseInt move-string-i0) 1)
			          move-string-j1 (do (print "\tTraek j1: ") (flush) (read-line))
				  j1 (- (Integer/parseInt move-string-j1) 1)
			          move-string-i1 (do (print "\tTraek i1: ") (flush) (read-line))
				  i1 (- (Integer/parseInt move-string-i1) 1)
				  move {:fra-koordinat [j0 i0] :til-koordinat [j1 i1]}
			        ]
			        (if (infektion-hjlp-div/move-valid? @board player-chip move)
				  (swap! board infektion-hjlp-div/make-move move)
				)
				(println (str "\n\tDit opdaterede braet spiller " player-number ":"))
	                        (println (str "\n" (infektion-hjlp-div/board-to-str @board "\t\t") "\n"))
				(str move)
			   )
		       )
	 update-board (fn [unit-input]
	                  (let [
		                 move-string (nth (:data unit-input) 1)
				 move (edn/read-string move-string)
			       ]
			       (if (infektion-hjlp-div/move-valid? @board opponent-chip move)
			         (swap! board infektion-hjlp-div/make-move move)
			       )
			  )
		      )
       ]
       (fn [unit-input]
         (let [
               first-data-element (first (:data unit-input))
             ]
             (case first-data-element
	        "initialiserSpil"	  (do
		                            (reset! board (infektion-hjlp-div/init-board "*" "¤"))
					    {:data ["Ok"]}
					  )
                "hentFoersteTraek"          {:data [(str (get-user-move player-number))]}
                "hentNaesteTraek"         (do
		                            (update-board unit-input)
		                            {:data [(str (get-user-move player-number))]}
					  )
                "meddelTraek"             (do
		                            (update-board unit-input)
					    (println (str "\n\tDit braet spiller " player-number ":"))
					    (println (str "\n" (infektion-hjlp-div/board-to-str @board "\t\t") "\n"))
					    {:data ["Accepteret"]}
					  )
             )
        )
      )
  )
)


(def spiller1 (new-player 1))
(def spiller2 (new-player 2))

