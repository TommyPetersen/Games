(ns games.infection.non-interactive-arbiter
  (:require [clojure.test :refer :all]
            (dk-aia-clojure [definitions :as aia-defs])
	    (games.infection [infection-utilities :as infection-utils])
    	    [clojure.string :as str]
	    [clojure.edn :as edn]
  )
)


    ;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;                   ;;;
    ;;;   * Infection *   ;;;
    ;;;                   ;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;


(def board (atom nil))
(def game-status (atom {}))

;;; Arbiter ;;;

(defn arbiter [unit-input]
  (let [first-data-element (first (:data unit-input))]
       (case first-data-element
         "init-game"           (do
	                         (reset! board (infection-utils/init-board "P1" "P2"))
	                         (reset! game-status {})
	                         {:data ["Ready"]}
                               )
         "new-move"            (let [
	                              player (nth (:data unit-input) 1)
				      other-player (if (= player "P1") "P2" "P1")
	                              move (edn/read-string (nth (:data unit-input) 2))
				    ]
				    (if (not (infection-utils/move-valid? @board player move))
				        (let [
					       real-move {
					                   :from-coord [(inc (first (:from-coord move))) (inc (second (:from-coord move)))]
							   :to-coord [(inc (first (:to-coord move))) (inc (second (:to-coord move)))]
							 }
					     ]
					     (reset! game-status {:disqualified player :illegal-move (str real-move)})
				             {:data [(str {:continuation-sign "-" :move (str move)})]}
					)

				        (do
					  (swap! board infection-utils/make-move move)
                                          (if (or (infection-utils/has-won? @board player)
					          (infection-utils/has-won? @board other-player)
					      )
				            (let [
					           real-move {
					                       :from-coord [(inc (first (:from-coord move))) (inc (second (:from-coord move)))]
					  	  	       :to-coord [(inc (first (:to-coord move))) (inc (second (:to-coord move)))]
							     }
					           winner (if (infection-utils/has-won? @board player) player other-player)
					         ]
				                 (reset! game-status {:winner winner :move (str player ": " real-move)})
                                                 {:data [(str {:continuation-sign "-" :move (str move)})]}
                                            )
				            {:data [(str {:continuation-sign "+" :move (str move)})]}
				          )
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
