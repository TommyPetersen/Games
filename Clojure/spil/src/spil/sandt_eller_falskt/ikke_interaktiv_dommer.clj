(ns spil.sandt-eller-falskt.ikke-interaktiv-dommer
  (:require [clojure.test :refer :all]
    	    [clojure.string :as str]
  )
)


    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;                        ;;;
    ;;; * Sandt eller falskt * ;;;
    ;;;                        ;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def praemie-vaerdier (atom {}))
(def spil-status (atom {}))

;;; Dommer ;;;

(defn dommer [enheds-inddata]
  (let [foerste-dataelement (first (:data enheds-inddata))]
       (case foerste-dataelement
         "initialiserSpil"     (do
	                         (reset! praemie-vaerdier {:S1 "SANDT" :S2 "SANDT"})
	                         (reset! spil-status {})
	                         {:data ["Ok"]}
                               )
         "nytTraek"            (let [
	                              spiller (nth (:data enheds-inddata) 1)
	                              spillertraek (nth (:data enheds-inddata) 2)
				    ]
				    (if (= (str/upper-case spillertraek) ((keyword spiller) @praemie-vaerdier))
				      (do
				        (reset! spil-status {:vinder (keyword spiller) :gevinst {:gevinstvaerdi @praemie-vaerdier}})
                                        {:data [(str {:fortsaettelsestegn "-" :traek spillertraek})]}
                                      )
				      {:data [(str {:fortsaettelsestegn "+" :traek spillertraek})]}
				    )
			       )
	 "hentStatus"          {:data ["Dommerstatus" (str @spil-status)]}

	 {:data ["Fejl i data"]}
       )
  )
)
