(ns games.connect-four.connect-four-utilities
  (:require (games [game-utilities :as game-utils]))
  (:import (java.awt.event MouseEvent))
)
;;
;;			*** TERMINOLOGY ***
;;
;; Board: [[...][...]...[...]] A number of columns of unknown height
;; Coordinate: [j i] The entry at column j (from left to right) and
;; row i (from bottom to top).
;;
;;
;;			*** FUNCTIONS ***
;;
(defn empty-board [board-width]
  (vec (repeat board-width []))
)

(defn next-index [[j i] direction]
  (cond (= direction :NW)		[(dec j) (inc i)]
	(= direction :N)		[j (inc i)]
	(= direction :NE)		[(inc j) (inc i)]
	(= direction :W)		[(dec j) i]
	(= direction :E)		[(inc j) i]
	(= direction :SW)		[(dec j) (dec i)]
	(= direction :S)		[j (dec i)]
	(= direction :SE)		[(inc j) (dec i)]
	:other			 	[j i]
  )
)

;;
;; Name:	lookup-strip
;; Input:
;;		board: A board.
;;		board-height: The board height.
;;		[j i]: Board coordinate of the beginning of the strip.
;;		direction: Compass direction (:NW, :N, :NE, :W, :E, :SW, :S, :SE).
;;		length: The length of the strip.
;; Output:
;;		A vector of symbols in the given strip. The returned
;;		vector is within board boundaries, so it's length may
;;		not always be the same as the given input length.
;;
(defn lookup-strip [board board-height [j i] direction length]
  (let [b (count board)]
    (loop [
            k 0
	    result []
	    [coord-j coord-i :as coord] [j i]
	  ]
	  (if (or (>= k length) (< coord-j 0) (>= coord-j b) (< coord-i 0) (>= coord-i board-height))
	      result
	      (recur
	        (inc k)
	        (conj result (game-utils/lookup board coord))
	        (next-index coord direction)
	      )
	  )
    )
  )
)

(defn column-valid? [board board-width board-height j]
  (and (>= j 0) (< j board-width) (< (count (nth board j)) board-height))
)

(defn valid-column-seq [board board-height]
  (let [board-width (count board)]
    (filter #(column-valid? board board-width board-height %) (range board-width))
  )
)

(defn get-random-valid-column [board board-height]
  (let [columns (valid-column-seq board board-height)]
       (nth columns (rand-int (count columns)))
  )
)

(defn insert [board j symbol]
  (if (and (>= j 0) (< j (count board)))
    (assoc board j (conj (nth board j) symbol))
    board
  )
)

;;
;; Name: count-symbols
;; Input:
;;		board: A board.
;;		j: Column number from left to right.
;;		direction: Compass direction (:NW, :N, :NE, :W, :E, :SW, :S, :SE).
;; Output:
;;		The number of identical symbols in a consecutive run
;;		originating in the coordinate as given by the top
;;		entry in column j and headed in the given direction.
;;
(defn count-symbols [board j direction]
  (let [i (if (and (>= j 0) (< j (count board)))
	    (dec (count (nth board j)))
	    -1
	    ),
	symbol (game-utils/lookup board [j i])]
    (loop [[k l] (next-index [j i] direction),
	   counter 0]
      (let [lookup-symbol (game-utils/lookup board [k l])]
	(if (or (= lookup-symbol nil)
		(not (= lookup-symbol symbol)))
	  counter
	  (recur (next-index [k l] direction)
		 (inc counter))
	)
      )
    )
  )
)

(defn has-won? [board j]
  (or (>= (+ (count-symbols board j :NW) (count-symbols board j :SE) 1) 4)
      (>= (+ (count-symbols board j :SW) (count-symbols board j :NE) 1) 4)
      (>= (+ (count-symbols board j :W) (count-symbols board j :E) 1) 4)
      (>= (+ (count-symbols board j :S) 1) 4)
  )
)

(defn has-symbol-won? [board j symbol]
  (and
    (= (game-utils/lookup board [j (dec (count (nth board j)))]) symbol)
    (has-won? board j)
  )
)

(defn is-full? [board board-height]
  (every? #(= (count %) board-height) board)
)

;;; VISUALIZATION ;;;
(defn find-column-number [x cell-coords]
  (let [pred #(and (>= x (:cell-left-border %)) (< x (:cell-right-border %)))]
       (:column-index (first (filter pred cell-coords)))
  )
)

(defn get-user-move [camera window-width window-height border-coords cell-coords]
  (let [insets (.getInsetsOnScreen camera)]
       (loop [mouse-event (.getCurrentMouseEventOnScreen camera)]
	     (if (and (not= nil mouse-event) (= (.getButton mouse-event) MouseEvent/BUTTON1))
	         (let [
		        transformed-coords (game-utils/transform-coords-in-mouse-event insets mouse-event window-width window-height)
			transformed-x (:transformed-x transformed-coords)
			transformed-y (:transformed-y transformed-coords)
		      ]
		      (if (and (>= transformed-x (:left border-coords)) (<= transformed-x (:right border-coords))
		               (>= transformed-y (:bottom border-coords)) (<= transformed-y (:top border-coords))
		          )
			  (find-column-number transformed-x cell-coords)
			  (do
                            (Thread/sleep 200)
                            (recur (.getCurrentMouseEventOnScreen camera))
	                  )
		      )
		 )
		 (do
                   (Thread/sleep 200)
                   (recur (.getCurrentMouseEventOnScreen camera))
	         )
	     )
       )
  )
)

(defn board-to-str [board board-height row-tabulator]
  (let [board-width (count board)]
    (if (= board-width 0)
      ""
      (loop [i (max (dec board-height) (dec (apply max (map count board)))),
	     board-str (str row-tabulator " 1 2 3 4 5 6 7 \n\n")]
	(if (< i 0)
	  (str board-str
	       "\n\n" row-tabulator " 1 2 3 4 5 6 7 "
	  )
	  (recur
	    (dec i)
	    (str board-str
	 	 (loop [j 0,
		        line-str ""]
		   (if (>= j board-width)
		     (str row-tabulator line-str "|")
		     (recur
		      (inc j)
		      (let [symbol (game-utils/lookup board [j i])]
		        (str line-str
			    (cond (not symbol) "| "
				  :other (str "|" symbol "")
				  )
			    )
		        )
		      )
		   )
		 )
		 (if (> i 0) "\n")
	    )
	  )
	)
      )
    )
  )
)

(defn rand-from-seq [num-seq]
  (let [sum (apply + num-seq)]
    (if (<= sum 0)
      nil
      (let [r (rand sum)]
	(loop [acc 0,
	       i 0,
	       val-i (nth num-seq 0)]
	  (if (< r (+ acc val-i))
	    i
	    (recur (+ acc val-i)
		   (inc i)
		   (nth num-seq (inc i))
	    )
	  )
	)
      )
    )
  )
)

;;; MINIMAX ;;;

(defn terminal? [board]
  (or
    (has-won? board 0)
    (has-won? board 1)
    (has-won? board 2)
    (has-won? board 3)
    (has-won? board 4)
    (has-won? board 5)
    (has-won? board 6)
    (is-full? board 6)
  )
)

(defn cutoff? [board ply-depth max-ply-depth]
  (or
    (terminal? board)
    (>= ply-depth max-ply-depth)
  )
)

(defn successor [board j symbol]
  (insert board j symbol)
)

(defn successors [board symbol]
  (for [j (valid-column-seq board 6)]
       [j (successor board j symbol)]
  )
)

(defn conquest-analysis [strip-vec]
  (if (not (= 4 (count strip-vec)))
    0
    (let [M-count (atom 0),
	  O-count (atom 0)]
      (doseq [entry strip-vec]
	(cond
	 (= entry "M") (swap! M-count inc)
	 (= entry "O") (swap! O-count inc)
	 )
	)
      (if (or
	    (and (= @M-count 0) (= @O-count 0))
	    (and (> @M-count 0) (> @O-count 0))
	  )
	  0
	  (if (> @M-count 0)
	    (cond
	      (= @M-count 1) 1
	      (= @M-count 2) 10
	      (= @M-count 3) 50
	    )
	    (cond
	      (= @O-count 1) -1
	      (= @O-count 2) -10
	      (= @O-count 3) -50
	    )
	  )
      )
    )
  )
)

(defn conquest-rate [board]
  (let [sum (atom 0)]
    (doseq [i (range 5 -1 -1),
	    j (range 7),
	    direction (list :NE :E :SE :S)]
      (let [
             strip (lookup-strip board 6 [j i] direction 4),
	     analysis-result (conquest-analysis strip)
	   ]
	   (swap! sum + analysis-result)
      )
    )
    @sum
  )
)

(defn evaluate [board turn]
  (cond
   (or
    (has-symbol-won? board 0 "M")
    (has-symbol-won? board 1 "M")
    (has-symbol-won? board 2 "M")
    (has-symbol-won? board 3 "M")
    (has-symbol-won? board 4 "M")
    (has-symbol-won? board 5 "M")
    (has-symbol-won? board 6 "M")
    )
   32767

   (or
    (has-symbol-won? board 0 "O")
    (has-symbol-won? board 1 "O")
    (has-symbol-won? board 2 "O")
    (has-symbol-won? board 3 "O")
    (has-symbol-won? board 4 "O")
    (has-symbol-won? board 5 "O")
    (has-symbol-won? board 6 "O")
    )
   -32767

   :other
   (let [bonus (if (= turn "M") 16 -16)]
     (+ (conquest-rate board) bonus)
     )
   )
  )

(declare max-val min-val)

(defn min-val [board alpha beta ply-depth max-ply-depth]
  (if (cutoff? board ply-depth max-ply-depth)
    {:value (* (/ 1 ply-depth) (evaluate board "O")), :leaf-count 1}
    (let [
           v (atom (Integer/MAX_VALUE))
	   beta-local (atom beta)
	   leaf-count (atom 0)
	   keep-searching (atom true)
	 ]
         (doseq [[succ-j succ-board] (successors board "O")]
	   (if @keep-searching
	     (let [max-result (max-val succ-board alpha @beta-local (inc ply-depth) max-ply-depth)]
	          (swap! v min (:value max-result))
	          (swap! leaf-count + (:leaf-count max-result))
	          (if (<= @v alpha) (reset! keep-searching false))
	          (swap! beta-local min @v)
	     )
	   )
	 )
         {:value @v, :leaf-count @leaf-count}
    )
  )
)

(defn max-val [board alpha beta ply-depth max-ply-depth]
  (if (cutoff? board ply-depth max-ply-depth)
    (let [valid-columns (valid-column-seq board 6)]
      {
        :action (if (> (count valid-columns) 0)
		  (nth valid-columns (int (rand (count valid-columns))))
		  -1
		)
        :value (* (/ 1 ply-depth) (evaluate board "M"))
        :leaf-count 1
      }
    )
    
    (let [
           v (atom (Integer/MIN_VALUE))
	   j (atom 0)
	   alpha-local (atom alpha)
	   leaf-count (atom 0)
	   keep-searching (atom true)
	 ]
         (doseq [[succ-j succ-board] (successors board "M")]
	   (if @keep-searching
	     (let [min-result (min-val succ-board @alpha-local beta (inc ply-depth) max-ply-depth)]
	       (if (> (:value min-result) @v)
	         (do
		   (reset! j succ-j)
		   (reset! v (:value min-result))
		 )
	       )
	       (swap! leaf-count + (:leaf-count min-result))
	       (if (>= @v beta) (reset! keep-searching false))
	       (swap! alpha-local max @v)
	     )
	   )
	 )
         {:action @j, :value @v, :leaf-count @leaf-count}
    )
  )
)

(defn alpha-beta-search [board max-ply-depth]
  (:action (max-val board (Integer/MIN_VALUE) (Integer/MAX_VALUE) 0 max-ply-depth))
)

(defn next-move [board max-ply-depth first-move? column-distribution]
  (if first-move?
    (rand-from-seq column-distribution)  ;Opening variation.
    (alpha-beta-search board max-ply-depth)
  )
)
