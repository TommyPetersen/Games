(ns spil.sandt-eller-falskt.interaktiv-tekstbaseret-spiller
  (:require [clojure.test :refer :all]
    	    [clojure.string :as str]
  )
)

    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;                        ;;;
    ;;; * Sandt eller falskt * ;;;
    ;;;                        ;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn hent-spillertraek []
  (print "\t[* Sandt eller falskt *] Indtast dit traek og tryk paa \"Enter\": ")
  (flush)
  (read-line)
)

(defn spiller-funktion [enheds-inddata]
  (let [foerste-dataelement (first (:data enheds-inddata))]
      (case foerste-dataelement
         "initialiserSpil"          {:data ["Ok"]}
         "hentFoersteTraek"         {:data [(hent-spillertraek)]}
         "hentNaesteTraek"          {:data [(hent-spillertraek)]}
         "meddelTraek"              {:data ["Accepteret"]}

	 {:data ["Fejl i data"]}
       )
  )
)

(defn spiller1 [enheds-inddata]
  (spiller-funktion enheds-inddata)
)

(defn spiller2 [enheds-inddata]
  (spiller-funktion enheds-inddata)
)


