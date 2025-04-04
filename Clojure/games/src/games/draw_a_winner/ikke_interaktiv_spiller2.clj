(ns games.draw-a-winner.ikke-interaktiv-spiller2
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


;;; Spiller2 ;;;

(defn spiller2 [unit-input]
  (let [first-data-element (first (:data unit-input))]
       (case first-data-element
         "initialiserSpil"          {:data ["Ok"]}
         "hentNaesteTraek"          {:data [(str (rand-int 6))]}
         "meddelTraek"              {:data ["Accepteret"]}

	 {:data ["Fejl i data"]}
       )
  )
)

