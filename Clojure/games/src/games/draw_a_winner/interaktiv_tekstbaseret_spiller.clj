(ns games.draw-a-winner.interaktiv-tekstbaseret-spiller
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

(defn get-user-move []
  (print "\tIndtast dit traek (1 - 6): ")
  (flush)
  (read-line)
)

(defn player [unit-input]
  (let [first-data-element (first (:data unit-input))]
      (case first-data-element
         "initialiserSpil"          {:data ["Ok"]}
         "hentFoersteTraek"         {:data [(str (- (Integer/parseInt (get-user-move)) 1))]}
         "hentNaesteTraek"          {:data [(str (- (Integer/parseInt (get-user-move)) 1))]}
         "meddelTraek"              {:data ["Accepteret"]}

	 {:data ["Fejl i data"]}
       )
  )
)

(defn spiller1 [unit-input]
  (player unit-input)
)

(defn spiller2 [unit-input]
  (player unit-input)
)


