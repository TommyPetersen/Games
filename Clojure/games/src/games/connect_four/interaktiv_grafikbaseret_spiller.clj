(ns games.connect-four.interaktiv-grafikbaseret-spiller
  (:require [clojure.test :refer :all]
	    (games [game-utilities-aiamg :as game-utils-aiamg]
	           [game-utilities-misc :as game-utils-misc])
            (games.connect-four [connect-four-utilities-aiamg :as connect-four-utils-aiamg]
                                [connect-four-utilities-misc :as connect-four-utils-misc])
    	    [clojure.string :as str]
  )
  (:import [java.util.concurrent.locks ReentrantLock])
)

    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;;;                      ;;;
    ;;; * Fire paa stribe  * ;;;
    ;;;                      ;;;
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn ny-spiller [spillernummer]
  (let [
         specialiserede-grafikmodul connect-four-utils-aiamg/specialiserede-grafikmodul
	 vinduesbredde 800
	 vindueshoejde 600
	 base-frame (game-utils-aiamg/calculate-base-frame vinduesbredde vindueshoejde)
         cell-grid-coords (game-utils-aiamg/generate-cell-grid-coords 7 6 base-frame)
         border-coords (:border-coords cell-grid-coords)
         cell-coords (:cell-coords cell-grid-coords)
	 braet (atom (connect-four-utils-misc/empty-board 7))
	 tidsenhed 1000
	 tidsgraense 120000
	 spillerbrik (if (= spillernummer 1) "*" "¤")
	 modstanderbrik (if (= spillernummer 1) "¤" "*")
	 continue-going-opponent (atom (atom false))
	 interrupt-get-move (atom false)
	 tegn-nedtaellinger (fn [
	                          samlet-tid-for-spiller
				  samlet-tid-for-modstander
				  tidsgraense
	                        ]
			      ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :kald-funktion-med-laas)
			        (fn []
		                  ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :samlet-tid-for-spiller samlet-tid-for-spiller)
				  ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :samlet-tid-for-modstander samlet-tid-for-modstander)
				  ((@(specialiserede-grafikmodul :funktionalitet) :rens-laerred-tegn-og-vis-alt))
				)
				[]
			      )
			    )
         hent-spillertraek (fn [spillernummer]
			   (let [
				  go-loop-result-player (game-utils-misc/go-loop-on-atom
				                          (fn [v]
					                    (tegn-nedtaellinger v 0 tidsgraense)
						          )
				                          tidsenhed tidsgraense interrupt-get-move
							)
			          continue-going (:continue-going go-loop-result-player)
			  	  j (connect-four-utils-aiamg/get-user-move specialiserede-grafikmodul interrupt-get-move)
			          _ (reset! continue-going false)
			        ]
			        (if (connect-four-utils-misc/column-valid? @braet 7 6 j)
				  (do
				    (swap! braet connect-four-utils-misc/insert j spillerbrik)
				    ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :kald-funktion-med-laas)
				      (fn []
				        ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :braet @braet)
				      )
				      []
				    )
				    (if (connect-four-utils-misc/is-not-full? @braet 6)
				      (let [
				             go-loop-result-opponent (game-utils-misc/go-loop-on-atom
                                                                       (fn [v]
								         (tegn-nedtaellinger 0 v tidsgraense)
								       )
                                                                       tidsenhed tidsgraense interrupt-get-move
								     )
				           ]
				           (reset! continue-going-opponent (:continue-going go-loop-result-opponent))
				      )
				      (tegn-nedtaellinger 0 0 tidsgraense)
				    )
				  )
				)
				j
			   )
		       )
	 update-board (fn [enheds-inddata]
	                  (let [
		                 move-string (nth (:data enheds-inddata) 1)
				 j (Integer/parseInt move-string)
			       ]
			       (reset! @continue-going-opponent false)
			       (if (connect-four-utils-misc/column-valid? @braet 7 6 j)
			         (do
				   (swap! braet connect-four-utils-misc/insert j modstanderbrik)
				   ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :kald-funktion-med-laas)
				     (fn []
				       ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :braet @braet)
				     )
				     []
				   )
				 )
			       )
			       ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :kald-funktion-med-laas)
			         (fn []
			           ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :valgte-celler-indekseret [{:row-index 5 :column-index j}])
			           ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :samlet-tid-for-spiller 0)
			           ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :samlet-tid-for-modstander 0)
				   ((@(specialiserede-grafikmodul :funktionalitet) :rens-laerred-tegn-og-vis-alt))
				   ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :valgte-celler-indekseret [])
				 )
				 []
			       )
			  )
		      )
       ]
       (fn [enheds-inddata]
         (let [first-data-element (first (:data enheds-inddata))]
             (case first-data-element
	        "initialiserSpil"	  (do
				            (reset! braet (connect-four-utils-misc/empty-board 7))
					    ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :kald-funktion-med-laas)
			                      (fn []
		                                ((@(specialiserede-grafikmodul :funktionalitet) :fastsaet-tilstand) @braet spillernummer vinduesbredde vindueshoejde 7 6 [50 10 85 0] [10 50 85 0] 120000)
					        ((@(specialiserede-grafikmodul :funktionalitet) :rens-laerred-tegn-og-vis-alt))
					      )
					      []
					    )
					    {:data ["Ok"]}
					  )
                "hentFoersteTraek"        {:data [(str (hent-spillertraek spillernummer))]}
                "hentNaesteTraek"         (do
		                            (update-board enheds-inddata)
		                            {:data [(str (hent-spillertraek spillernummer))]}
					  )
                "meddelTraek"             (do
		                            (update-board enheds-inddata)
					    {:data ["Accepteret"]}
					  )
					  
		{:data ["Fejl i data"]}
             )
        )
      )
  )
)


(def spiller1 (ny-spiller 1))
(def spiller2 (ny-spiller 2))

