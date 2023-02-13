(ns games.draw-a-winner.non-interactive-player2
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


;;; Player2 ;;;

(defn player2 [unit-input]
  (let [first-data-element (first (:data unit-input))]
       (case first-data-element
         "initGame"                {:data ["Ok"]}
         "getNextMove"             {:data [(str (+ 1 (rand-int 6)))]}
         "notifyMove"              {:data ["Accepted"]}

	 {:data ["Error in data"]}
       )
  )
)


;;; TESTS ;;;

(deftest unit-test
  (testing "Unit"
    (let [a 1]
      (is (= a 1))
    )
  )
)
