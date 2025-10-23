(ns games.infection.interaktiv-grafikbaseret-spiller
  (:require [clojure.test :refer :all]
            (games [game-utilities-aiamg :as game-utils-aiamg]
	           [game-utilities-misc :as game-utils-misc])
	    (games.infection [infection-utilities-aiamg :as infection-utils-aiamg]
	                     [infection-utilities-misc :as infection-utils-misc])
    	    [clojure.string :as str]
	    [clojure.edn :as edn]
  )
  (:import [java.util.concurrent.locks ReentrantLock])
)


    ;;;;;;;;;;;;;;;;;;;;;;
    ;;;                ;;;
    ;;; * Infection  * ;;;
    ;;;                ;;;
    ;;;;;;;;;;;;;;;;;;;;;;


(defn ny-spiller [spillernummer]
  (let [
         specialiserede-grafikmodul infection-utils-aiamg/specialiserede-grafikmodul
         camera (atom nil)
	 vinduesbredde 800
	 vindueshoejde 600
	 base-frame (game-utils-aiamg/calculate-base-frame vinduesbredde vindueshoejde)
         cell-grid-coords (game-utils-aiamg/generate-cell-grid-coords 7 7 base-frame)
         border-coords (:border-coords cell-grid-coords)
         cell-coords (:cell-coords cell-grid-coords)
         braet (atom (infection-utils-misc/init-board "*" "¤"))
	 braethistorik (atom [@braet])
	 tidsenhed 1000
         tidsgraense 120000
	 selected-cell-indexes (atom nil)
	 mouse-over-cell-index (atom nil)
	 mouse-over-cell-frame-color (atom nil)
	 historiklaengde 15
	 stats-frame (game-utils-aiamg/calculate-aux-frame (:left border-coords) (+ (:left border-coords) (* 20 historiklaengde)) (:base-frame-top-border base-frame) (:top border-coords) [0 0 10 10])
	 countdown-frame-left (game-utils-aiamg/calculate-aux-frame (:base-frame-left-border base-frame) (:left border-coords) (:top border-coords) (:bottom border-coords) [50 10 85 0])
         countdown-frame-right (game-utils-aiamg/calculate-aux-frame (:right border-coords) (:base-frame-right-border base-frame) (:top border-coords) (:bottom border-coords) [10 50 85 0])
	 countdown-frame-player (if (= spillernummer 1) countdown-frame-left countdown-frame-right)
	 countdown-frame-opponent  (if (= spillernummer 1) countdown-frame-right countdown-frame-left)
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
			     (if (infection-utils-misc/cannot-move? @braet spillerbrik)
			       (do
			         (println (str "\tIngen mulige traek paa braettet...melder pas"))
			         (str {:from-coord [-1 -1] :to-coord [-1 -1]})
			       )
			       (let [
				      go-loop-result-player (game-utils-misc/go-loop-on-atom
				                              (fn [v]
					                        (tegn-nedtaellinger v 0 tidsgraense)
						              )
				                              tidsenhed tidsgraense interrupt-get-move)
			              continue-going (:continue-going go-loop-result-player)
			              move (infection-utils-aiamg/get-user-move specialiserede-grafikmodul interrupt-get-move)
			              _ (reset! continue-going false)
				    ]
			            (if (infection-utils-misc/move-valid? @braet spillerbrik move)
				      (do
				        (swap! braet infection-utils-misc/make-move move)
				        (swap! braethistorik conj @braet)
					((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :kald-funktion-med-laas)
					  (fn []
					    ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :braet @braet)
					    ((@(specialiserede-grafikmodul :funktionalitet) :opdater-tilstand) :braethistorik @braethistorik)
					  )
					  []
					)
				        (if (infection-utils-misc/can-move? @braet modstanderbrik)
				          (let [
				                 go-loop-result-opponent (game-utils-misc/go-loop-on-atom
                                                                           (fn [v]
									     (tegn-nedtaellinger 0 v tidsgraense)
								           )
                                                                           tidsenhed tidsgraense interrupt-get-move)
				               ]
                                               (reset! continue-going-opponent (:continue-going go-loop-result-opponent))
				          )
					  (tegn-nedtaellinger 0 0 tidsgraense)
				        )
				      )
				    )
				    (str move)
			       )
			     )
		         )
	 update-board (fn [unit-input]
	                  (let [
		                 move-string (nth (:data unit-input) 1)
				 move (edn/read-string move-string)
				 from-cell {:row-index (second (:from-coord move)) :column-index (first (:from-coord move))}
				 to-cell {:row-index (second (:to-coord move)) :column-index (first (:to-coord move))}
			       ]
			       (reset! @continue-going-opponent false)
			       (if (infection-utils-misc/move-valid? @braet modstanderbrik move)
			         (do
			           (swap! braet infection-utils-misc/make-move move)
				   (swap! braethistorik conj @braet)
				   ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :kald-funktion-med-laas)
				     (fn []
				       ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :braet @braet)
				       ((@(specialiserede-grafikmodul :funktionalitet) :opdater-tilstand) :braethistorik @braethistorik)
				     )
				     []
				   )
				 )
			       )
			       ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :kald-funktion-med-laas)
			         (fn []
			           ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :opdater-tilstand) :valgte-celler-indekseret [{:row-index (second (:from-coord move)) :column-index (first (:from-coord move))} {:row-index (second (:to-coord move)) :column-index (first (:to-coord move))}])
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
		                            (reset! braet (infection-utils-misc/init-board "*" "¤"))
					    (reset! braethistorik [@braet])
					    ((@(((specialiserede-grafikmodul :forfaedre) :gaengse-grafikmodul) :funktionalitet) :kald-funktion-med-laas)
			                      (fn []
		                                ((@(specialiserede-grafikmodul :funktionalitet) :fastsaet-tilstand) @braet spillernummer vinduesbredde vindueshoejde 7 7 [50 10 85 0] [10 50 85 0] 120000 historiklaengde @braethistorik)
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
             )
        )
      )
  )
)


(def spiller1 (ny-spiller 1))
(def spiller2 (ny-spiller 2))
