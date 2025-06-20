(ns games.connect-four.interaktiv-tekstbaseret-spiller
  (:require [clojure.test :refer :all]
            (games.connect-four [connect-four-utilities-misc :as connect-four-utils-misc])
    	    [clojure.string :as str]
  )
)


    ;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;                   ;;;
    ;;; * Connect four  * ;;;
    ;;;                   ;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;


(defn new-player [player-number]
  (let [
         board (atom (connect-four-utils-misc/empty-board 7))
	 player-chip (if (= player-number 1) "*" "¤")
	 opponent-chip (if (= player-number 1) "¤" "*")
         get-user-move (fn [player-number]
	                   (println (str "\n\tDit braet spiller " player-number ":"))
	                   (println (str "\n" (connect-four-utils-misc/board-to-str @board 6 "\t\t") "\n"))
	                   (print (str "\tSpiller" player-number ", indtast dit traek (1 - 7): "))
			   (flush)
			   (let [
			          move-string (read-line)
				  j (- (Integer/parseInt move-string) 1)
			        ]
			        (if (connect-four-utils-misc/column-valid? @board 7 6 j)
				  (swap! board connect-four-utils-misc/insert j player-chip)
				)
				(println (str "\n\tDit opdaterede braet spiller " player-number ":"))
	                        (println (str "\n" (connect-four-utils-misc/board-to-str @board 6 "\t\t") "\n"))
				(str j)
			   )
		       )
	 update-board (fn [unit-input]
	                  (let [
		                 move-string (nth (:data unit-input) 1)
				 j (Integer/parseInt move-string)
			       ]
			       (if (connect-four-utils-misc/column-valid? @board 7 6 j)
			         (swap! board connect-four-utils-misc/insert j opponent-chip)
			       )
			  )
		      )
       ]
       (fn [unit-input]
         (let [first-data-element (first (:data unit-input))]
             (case first-data-element
	        "initialiserSpil"	  (do
		                            (reset! board (connect-four-utils-misc/empty-board 7))
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
					    (println (str "\n" (connect-four-utils-misc/board-to-str @board 6 "\t\t") "\n"))
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

