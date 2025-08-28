(ns spil.sandt-eller-falskt.ikke-interaktiv-spiller1
  (:require [clojure.test :refer :all]
    	    [clojure.string :as str]
  )
)


    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;                        ;;;
    ;;; * Sandt eller falskt * ;;;
    ;;;                        ;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;; Spiller1 ;;;

(defn spiller1 [enheds-inddata]
  (let [foerste-dataelement (first (:data enheds-inddata))]
       (case foerste-dataelement
         "initialiserSpil"          {:data ["Ok"]}
         "hentNaesteTraek"          {:data [(nth ["falskt" "sandt"] (rand-int 2))]}
         "hentFoersteTraek"         {:data [(nth ["falskt" "sandt"] (rand-int 2))]}
         "meddelTraek"              {:data ["Accepteret"]}

         {:data ["Fejl i data"]}
       )
  )
)

