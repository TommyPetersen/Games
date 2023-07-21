(ns games.infection.interactive-text-player
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
	                   (println (str "\n\tYour board player " player-number ":"))
	                   (println (str "\n" (infection-utils-misc/board-to-str @board "\t\t") "\n"))
	                   (println (str "\tPlayer" player-number ", enter your move: "))
			   (flush)
			   (let [
			          move-string-j0 (do (print "\tMove j0: ") (flush) (read-line))
				  j0 (- (Integer/parseInt move-string-j0) 1)
			          move-string-i0 (do (print "\tMove i0: ") (flush) (read-line))
				  i0 (- (Integer/parseInt move-string-i0) 1)
			          move-string-j1 (do (print "\tMove j1: ") (flush) (read-line))
				  j1 (- (Integer/parseInt move-string-j1) 1)
			          move-string-i1 (do (print "\tMove i1: ") (flush) (read-line))
				  i1 (- (Integer/parseInt move-string-i1) 1)
				  move {:from-coord [j0 i0] :to-coord [j1 i1]}
			        ]
			        (if (infection-utils-misc/move-valid? @board player-chip move)
				  (swap! board infection-utils-misc/make-move move)
				)
				(println (str "\n\tYour updated board player " player-number ":"))
	                        (println (str "\n" (infection-utils-misc/board-to-str @board "\t\t") "\n"))
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
         (let [
               first-data-element (first (:data unit-input))
             ]
             (case first-data-element
	        "init-game"		  (do
		                            (reset! board (infection-utils-misc/init-board "*" "¤"))
					    {:data ["Ok"]}
					  )
                "get-first-move"          {:data [(str (get-user-move player-number))]}
                "get-next-move"           (do
		                            (update-board unit-input)
		                            {:data [(str (get-user-move player-number))]}
					  )
                "notify-move"             (do
		                            (update-board unit-input)
					    (println (str "\n\tYour board player " player-number ":"))
					    (println (str "\n" (infection-utils-misc/board-to-str @board "\t\t") "\n"))
					    {:data ["Accepted"]}
					  )
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
