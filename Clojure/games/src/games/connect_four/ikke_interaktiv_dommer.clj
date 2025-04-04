(ns games.connect-four.ikke-interaktiv-dommer
  (:require [clojure.test :refer :all]
            (dk-aia-clojure [definitions :as aia-defs])
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

;;; Dommer ;;;

(defn dommer [unit-input]
  (let [first-data-element (first (:data unit-input))]
       (case first-data-element
         "initialiserSpil"   (do
	                         (reset! board (connect-four-utils-misc/empty-board 7))
				 (reset! game-status {})
	                         {:data ["Ok"]}
                               )
         "nytTraek"            (let [
	                              player (nth (:data unit-input) 1)
	                              j (Integer/parseInt (nth (:data unit-input) 2))
				    ]
				    (if (not (connect-four-utils-misc/column-valid? @board 7 6 j))
				        (do
					  (reset! game-status {:diskvalificeret (keyword player) :ugyldigt-traek {:traek j :base "nul-baseret"}})
				          {:data [(str {:fortsaettelsestegn "-" :traek (str j)})]}
					)

				        (do
					  (swap! board connect-four-utils-misc/insert j (keyword player))
                                          (if (connect-four-utils-misc/has-won? @board j)
				            (do
				              (reset! game-status {:vinder (keyword player) :traek {:traek j :base "nul-baseret"}})
                                              {:data [(str {:fortsaettelsestegn "-" :traek (str j)})]}
                                            )
					    (if (connect-four-utils-misc/is-full? @board 6)
					      (do
					        (reset! game-status {:vinder :UAFGJORT :traek {:traek j :base "nul-baseret"}})
                                                {:data [(str {:fortsaettelsestegn "-" :traek (str j)})]}
					      )
				              {:data [(str {:fortsaettelsestegn "+" :traek (str j)})]}
					    )
				          )
					)
				     )
			       )
	 "hentStatus"          {:data ["Dommerstatus" (str @game-status)]}

	 {:data ["Fejl i data"]}
       )
  )
)

