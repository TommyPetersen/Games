(ns games.infection.ikke-interaktiv-dommer
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

;;; Dommer ;;;

(defn dommer [unit-input]
  (let [first-data-element (first (:data unit-input))]
       (case first-data-element
         "initialiserSpil"     (do
	                         (reset! board (infection-utils-misc/init-board "S1" "S2"))
	                         (reset! game-status {})
	                         {:data ["Ok"]}
                               )
         "nytTraek"            (let [
	                              player (nth (:data unit-input) 1)
				      other-player (if (= player "S1") "S2" "S1")
	                              move (edn/read-string (nth (:data unit-input) 2))
				      traek (str/replace (str/replace move "from-coord" "fra-koordinat") "to-coord" "til-koordinat")
				    ]
				    (if (not (infection-utils-misc/move-valid? @board player move))
				        (do
					  (reset! game-status {:diskvalificeret (keyword player) :ugyldigt-traek {:traek traek :base "nul-baseret"}})
				          {:data [(str {:fortsaettelsestegn "-" :traek (str move)})]}
					)

				        (do
					  (swap! board infection-utils-misc/make-move move)
                                          (if (or (infection-utils-misc/has-won? @board player)
					          (infection-utils-misc/has-won? @board other-player)
					      )
				            (let [winner (if (infection-utils-misc/has-won? @board player) player other-player)]
				                 (reset! game-status {:vinder winner :traek {:traek (str player ": " traek) :base "nul-baseret"}})
                                                 {:data [(str {:fortsaettelsestegn "-" :traek (str move)})]}
                                            )
				            {:data [(str {:fortsaettelsestegn "+" :traek (str move)})]}
				          )
					)
				     )
			       )
	 "hentStatus"          {:data ["Dommerstatus" (str @game-status)]}

	 {:data ["Fejl i data"]}
       )
  )
)


