(ns games.connect-four.non-interactive-arbiter
  (:require [clojure.test :refer :all]
            (games.connect-four [connect-four-utilities-misc :as connect-four-utils-misc])
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
	                         (reset! board (connect-four-utils-misc/empty-board 7))
				 (reset! game-status {})
	                         {:data ["Ok"]}
                               )
         "new-move"            (let [
	                              player (nth (:data unit-input) 1)
	                              j (Integer/parseInt (nth (:data unit-input) 2))
				    ]
				    (if (not (connect-four-utils-misc/column-valid? @board 7 6 j))
				        (do
					  (reset! game-status {:disqualified (keyword player) :illegal-move {:column-no j :base "zero-based"}})
				          {:data [(str {:continuation-sign "-" :move (str j)})]}
					)

				        (do
					  (swap! board connect-four-utils-misc/insert j (keyword player))
                                          (if (connect-four-utils-misc/has-won? @board j)
				            (do
				              (reset! game-status {:winner (keyword player) :move {:column-no j :base "zero-based"}})
                                              {:data [(str {:continuation-sign "-" :move (str j)})]}
                                            )
					    (if (connect-four-utils-misc/is-full? @board 6)
					      (do
					        (reset! game-status {:winner :DRAW :move {:column-no j :base "zero-based"}})
                                                {:data [(str {:continuation-sign "-" :move (str j)})]}
					      )
				              {:data [(str {:continuation-sign "+" :move (str j)})]}
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
