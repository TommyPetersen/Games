(ns games.game-utilities-misc
  (:require [clojure.core.async :refer [go]])
)

(defn lookup [board [j i]]
  (try
    (nth (nth board j) i)
    (catch Exception E nil)
  )
)

(defn go-loop-on-atom [
          	        loopfunction		; The function which is called in the loop
                        time-unit  	      	; The time-unit expressed in milliseconds
			time-limit	      	; The time limit expressed in milliseconds
			interrupt-get-move    	; Atom deciding if get-user-move should be cancelled
                      ]
  (let [
         continue-going (atom true)
	 total-time-used (atom 0)
       ]
       (go
         (loop [
                 acc-time 0
	       ]
	       (loopfunction acc-time)
	       (Thread/sleep time-unit)
	       (if (and (<= acc-time time-limit) @continue-going)
	         (recur
	                (+ acc-time time-unit)
	         )
		 (do
	           (reset! total-time-used acc-time)
		   (reset! interrupt-get-move (> @total-time-used time-limit))
		 )
	       )
         )
       )
       {:continue-going continue-going :total-time-used @total-time-used}
  )
)

