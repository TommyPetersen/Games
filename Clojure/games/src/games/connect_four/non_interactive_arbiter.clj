(ns games.connect-four.non-interactive-arbiter
  (:require [clojure.test :refer :all]
            (dk-aia-clojure [definitions :as aia-defs])
            (games.connect-four [connect-four-utilities :as connect-four-utils])
    	    [clojure.string :as str]
  )
)


    ;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;                   ;;;
    ;;; * Connect four *  ;;;
    ;;;                   ;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;


(def board (atom nil))
(def game-status (atom {}))

;;; Arbiter ;;;

(defn arbiter [unit-input]
  (let [first-data-element (first (:data unit-input))]
       (case first-data-element
         "init-game"           (do
	                         (reset! board (connect-four-utils/empty-board 7))
				 (reset! game-status {})
	                         {:data ["Ready"]}
                               )
         "new-move"            (let [player (nth (:data unit-input) 1)
	                             move (Integer/parseInt (nth (:data unit-input) 2))
				     j (- move 1)]
				    (if (not (connect-four-utils/column-valid? @board 7 6 j))
				        (do
					  (reset! game-status {:disqualified (keyword player) :illegal-move (str move)})
				          {:data [(str {:continuation-sign "-" :move (str move)})]}
					)

				        (do
					  (swap! board connect-four-utils/insert j (keyword player))
                                          (if (connect-four-utils/has-won? @board j)
				            (do
				              (reset! game-status {:winner (keyword player) :move (str move)})
                                              {:data [(str {:continuation-sign "-" :move (str move)})]}
                                            )
					    (if (connect-four-utils/is-full? @board 6)
					      (do
					        (reset! game-status {:winner :DRAW :move (str move)})
                                                {:data [(str {:continuation-sign "-" :move (str move)})]}
					      )
				              {:data [(str {:continuation-sign "+" :move (str move)})]}
					    )
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
