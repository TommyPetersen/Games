(ns games.draw-a-winner.ikke-interaktiv-spiller1
  (:require [clojure.test :refer :all]
    	    [clojure.string :as str]
  )
)


    ;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;                   ;;;
    ;;; * Draw-A-Winner * ;;;
    ;;;                   ;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;


;;; Spiller1 ;;;

(defn spiller1 [unit-input]
  (let [first-data-element (first (:data unit-input))]
       (case first-data-element
         "initialiserSpil"          {:data ["Ok"]}
         "hentNaesteTraek"          {:data [(str (rand-int 6))]}
         "hentFoersteTraek"         {:data [(str (rand-int 6))]}
         "meddelTraek"              {:data ["Accepteret"]}

         {:data ["Fejl i data"]}
       )
  )
)

