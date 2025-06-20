(ns games.infection.non-interactive-arbiter
  (:require [clojure.test :refer :all]
	    (games.infection [infection-utilities-misc :as infection-utils-misc])
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
	                         (reset! board (infection-utils-misc/init-board "P1" "P2"))
	                         (reset! game-status {})
	                         {:data ["Ok"]}
                               )
         "new-move"            (let [
	                              player (nth (:data unit-input) 1)
				      other-player (if (= player "P1") "P2" "P1")
	                              move (edn/read-string (nth (:data unit-input) 2))
				    ]
				    (if (not (infection-utils-misc/move-valid? @board player move))
				        (do
					  (reset! game-status {:disqualified (keyword player) :illegal-move {:chosen-move (str move) :base "zero-based"}})
				          {:data [(str {:continuation-sign "-" :move (str move)})]}
					)

				        (do
					  (swap! board infection-utils-misc/make-move move)
                                          (if (or (infection-utils-misc/has-won? @board player)
					          (infection-utils-misc/has-won? @board other-player)
					      )
				            (let [winner (if (infection-utils-misc/has-won? @board player) player other-player)]
				                 (reset! game-status {:winner winner :move {:chosen-move (str player ": " move) :base "zero-based"}})
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
