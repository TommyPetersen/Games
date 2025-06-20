(ns games.draw-a-winner.non-interactive-player1
  (:require [clojure.test :refer :all]
    	    [clojure.string :as str]
  )
)


    ;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;                   ;;;
    ;;; * Draw-A-Winner * ;;;
    ;;;                   ;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;


;;; Player1 ;;;

(defn player1 [unit-input]
  (let [first-data-element (first (:data unit-input))]
       (case first-data-element
         "init-game"                {:data ["Ok"]}
         "get-first-move"           {:data [(str (rand-int 6))]}
         "get-next-move"            {:data [(str (rand-int 6))]}
         "notify-move"              {:data ["Accepted"]}

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
