(ns spil.sandt-eller-falskt.ikke-interaktiv-spiller2
  (:require [clojure.test :refer :all]
    	    [clojure.string :as str]
  )
)


    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;                        ;;;
    ;;; * Sandt eller falskt * ;;;
    ;;;                        ;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;; Spiller2 ;;;

(defn spiller2 [enheds-inddata]
  (let [foerste-dataelement (first (:data enheds-inddata))]
       (case foerste-dataelement
         "initialiserSpil"          {:data ["Ok"]}
         "hentNaesteTraek"          {:data [(nth ["falskt" "sandt"] (rand-int 2))]}
         "meddelTraek"              {:data ["Accepteret"]}

	 {:data ["Fejl i data"]}
       )
  )
)

