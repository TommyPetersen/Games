(ns games.draw-a-winner.non-interactive-arbiter
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


(def prize-numbers (atom {}))
(def game-status (atom {}))

;;; Arbiter ;;;

(defn arbiter [unit-input]
  (let [first-data-element (first (:data unit-input))]
       (case first-data-element
         "initGame"            (do
	                         (reset! prize-numbers {:P1 (str (+ 1 (rand-int 6))) :P2 (str (+ 1 (rand-int 6)))})
	                         {:data ["Ready"]}
                               )
         "newMove"             (let [player (nth (:data unit-input) 1)
	                             move (nth (:data unit-input) 2)]
				    (if (= move ((keyword player) @prize-numbers))
				      (do
				        (reset! game-status {:winner (keyword player) :prize-numbers @prize-numbers})
                                        {:data [(str {:continuation-sign "-" :move move})]}
                                      )
				      {:data [(str {:continuation-sign "+" :move move})]}
				    )
			       )
	 "getStatus"           {:data [(str @game-status)]}

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
