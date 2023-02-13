(ns games.draw-a-winner.interactive-text-player
  (:require [clojure.test :refer :all]
            (dk-aia-clojure [definitions :as aia-defs])
    	    [clojure.string :as str]
  )
)


    ;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;                   ;;;
    ;;; * Draw-A-Winner * ;;;
    ;;;                   ;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;


(defn get-user-move []
  (print "\tEnter your move (1 - 6): ")
  (flush)
  (read-line)
)


(defn player [unit-input]
  (let [first-data-element (first (:data unit-input))]
      (case first-data-element
         "initGame"                {:data ["Ok"]}
         "getFirstMove"            {:data [(str (get-user-move))]}
         "getNextMove"             {:data [(str (get-user-move))]}
         "notifyMove"              {:data ["Accepted"]}

	 {:data "Error in data"}
       )
  )
)


(defn player1 [unit-input]
  (player unit-input)
)


(defn player2 [unit-input]
  (player unit-input)
)


;;; TESTS ;;;

(deftest unit-test
  (testing "Unit"
    (let [a 1]
      (is (= a 1))
    )
  )
)
