(ns games.draw-a-winner.ikke-interaktiv-dommer
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

;;; Dommer ;;;

(defn dommer [unit-input]
  (let [first-data-element (first (:data unit-input))]
       (case first-data-element
         "initialiserSpil"     (do
	                         (reset! prize-numbers {:S1 (str (rand-int 6)) :S2 (str (rand-int 6))})
	                         (reset! game-status {})
	                         {:data ["Ok"]}
                               )
         "nytTraek"            (let [
	                              player (nth (:data unit-input) 1)
	                              move-str (nth (:data unit-input) 2)
				      move-int (Integer/parseInt move-str)
				    ]
				    (if (or (< move-int 0) (> move-int 5))
				      (do
				        (reset! game-status {:diskvalificeret (keyword player) :ugyldigt-traek {:traek move-int :base "nul-baseret"}})
					{:data [(str {:fortsaettelsestegn "-" :traek move-str})]}
				      )
				      (if (= move-str ((keyword player) @prize-numbers))
				        (do
				          (reset! game-status {:vinder (keyword player) :gevinst {:gevinsttal @prize-numbers :base "nul-baseret"}})
                                          {:data [(str {:fortsaettelsestegn "-" :traek move-str})]}
                                        )
				        {:data [(str {:fortsaettelsestegn "+" :traek move-str})]}
				      )
				    )
			       )
	 "hentStatus"          {:data ["Dommerstatus" (str @game-status)]}

	 {:data ["Fejl i data"]}
       )
  )
)
