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
         "init-game"           (do
	                         (reset! prize-numbers {:P1 (str (rand-int 6)) :P2 (str (rand-int 6))})
	                         (reset! game-status {})
	                         {:data ["Ok"]}
                               )
         "new-move"            (let [
	                              player (nth (:data unit-input) 1)
	                              move-str (nth (:data unit-input) 2)
				      move-int (Integer/parseInt move-str)
				    ]
				    (if (or (< move-int 0) (> move-int 5))
				      (do
				        (reset! game-status {:disqualified (keyword player) :illegal-move move-str})
					{:data [(str {:continuation-sign "-" :move move-str})]}
				      )
				      (if (= move-str ((keyword player) @prize-numbers))
				        (do
				          (reset! game-status {:winner (keyword player) :prize-numbers @prize-numbers})
                                          {:data [(str {:continuation-sign "-" :move move-str})]}
                                        )
				        {:data [(str {:continuation-sign "+" :move move-str})]}
				      )
				    )
			       )
	 "get-status"          {:data [(str @game-status)]}

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
