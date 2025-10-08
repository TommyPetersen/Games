(ns games.infection.infection-utilities-misc
  (:require (games [game-utilities-misc :as game-utils-misc]))
)

(defn update-cell [board [j i] symbol]
  (assoc board j (assoc (board j) i symbol))
)

(defn update-cells [board cells]
  (loop [
          var-board board
          var-cells cells
	]
	(if (= (count var-cells) 0)
	  var-board
	  (recur
	    (update-cell var-board (:coord (first var-cells)) (:symbol (first var-cells)))
	    (rest var-cells)
	  )
	)
  )
)

(defn init-board [player1-symbol player2-symbol]
  (let [empty-board (vec (repeat 7 (vec (repeat 7 nil))))]
       (update-cells empty-board [
                                   {:coord [0 0] :symbol player1-symbol} {:coord [6 6] :symbol player1-symbol}
				   {:coord [0 6] :symbol player2-symbol} {:coord [6 0] :symbol player2-symbol}
				 ]
       )
  )
)

(defn next-coord [[j i] direction]
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

(defn lookup-strip [board [j i] direction length]
  (loop [
          k 0
	  result []
	  [coord-j coord-i :as coord] [j i]
	]
	(if (or (>= k length) (< coord-j 0) (>= coord-j 7) (< coord-i 0) (>= coord-i 7))
	    result
	    (recur
	      (inc k)
	      (conj result (game-utils-misc/lookup board coord))
	      (next-coord coord direction)
	    )
        )
  )
)

(defn get-all-coords-with-given-symbol [board symbol]
  (for [
         j (range 7)
	 i (range 7)
	 :when (= (game-utils-misc/lookup board [j i]) symbol)
       ]
       [j i]
  )
)

(defn get-all-coords-with-a-symbol [board]
  (for [
         j (range 7)
	 i (range 7)
	 :when (not= (game-utils-misc/lookup board [j i]) nil)
       ]
       [j i]
  )
)

(defn get-neighbourhood [board [j i] radius]
  (let [
	 min-j (max 0 (- j radius))
	 max-j (min 6 (+ j radius))
	 min-i (max 0 (- i radius))
	 max-i (min 6 (+ i radius))
         top-left-corner  [min-j max-i]
	 bottom-right-corner [max-j min-i]
       ]
       (vec
         (for [
	        var-j (range min-j (+ max-j 1))
                var-i (range max-i (- min-i 1) -1)
	      ]
	      [var-j var-i]
         )
       )
  )
)

(defn valid-move-seq [board symbol]
  (let [board-move-seq (for [
                              from-coord (get-all-coords-with-given-symbol board symbol)
	                      to-coord (get-neighbourhood board from-coord 2)
	                      :when (= (game-utils-misc/lookup board to-coord) nil)
                            ]
                            {
                              :from-coord from-coord
	                      :to-coord to-coord
                            }
		       )
       ]
       (if (> (count board-move-seq ) 0)
           board-move-seq
	   (seq [{:from-coord [-1 -1] :to-coord [-1 -1]}])
       )
  )
)

(defn move-valid? [board symbol move]
  (let [
         valid-move-seq (valid-move-seq board symbol)
       ]
       (boolean (some #(= % move) valid-move-seq))
  )
)

(defn get-random-valid-move [board symbol]
  (let [moves (valid-move-seq board symbol)]
       (nth moves (rand-int (count moves)))
  )
)

(defn get-neighbourhood-infected-by-others [board center-coord radius]
  (let [
         center-symbol (game-utils-misc/lookup board center-coord)
	 neighbourhood-coords (get-neighbourhood board center-coord 1)
       ]
       (vec
         (for [
                neighbourhood-coord neighbourhood-coords
  	        neighbourhood-symbol (game-utils-misc/lookup board neighbourhood-coord)
	        :when (and (not= neighbourhood-symbol nil)
	                   (not= neighbourhood-symbol center-symbol))
              ]
              neighbourhood-coord
         )
       )
  )
)

(defn make-move [board move]
  (if (= move {:from-coord [-1 -1] :to-coord [-1 -1]})
      board
      (let [
             from-coord (:from-coord move)
	     to-coord (:to-coord move)
	     symbol (game-utils-misc/lookup board from-coord)
	     is-spreading? (and (< (Math/abs (- (first from-coord) (first to-coord))) 2)
	                        (< (Math/abs (- (second from-coord) (second to-coord))) 2))
	     cell-updates (if is-spreading? [{:coord to-coord :symbol symbol}]
	                                    [{:coord from-coord :symbol nil} {:coord to-coord :symbol symbol}])
	     neighbourhood-infected-by-others (get-neighbourhood-infected-by-others board to-coord 1)
	     neighbourhood-updates (vec (for [coord neighbourhood-infected-by-others] {:coord coord :symbol symbol}))
           ]
           (-> board
             (update-cells cell-updates)
             (update-cells neighbourhood-updates)
           )
      )
  )
)

(defn count-symbol [board symbol]
  (count (get-all-coords-with-given-symbol board symbol))
)

(defn count-symbols-in-board [
                               board
		               symbol-str1
		               symbol-str2
		             ]
  {
    (keyword symbol-str1) (count-symbol board symbol-str1)
    (keyword symbol-str2) (count-symbol board symbol-str2)
  }
)

(defn count-symbols-in-boards [
                                boards 
		                symbol-str1
				symbol-str2
			      ]
  (let [
         partial-symbol-count #(count-symbols-in-board % symbol-str1 symbol-str2)
       ]
       (vec (map partial-symbol-count boards))
  )
)

(defn is-full? [board]
  (every? #(not= % nil) (apply concat board))
)

(defn has-won? [board symbol]
  (or
    (and (is-full? board) (>= (count-symbol board symbol) 25))
    (and (=
           (count (get-all-coords-with-given-symbol board symbol))
	   (count (get-all-coords-with-a-symbol board))
	 )
    )
  )
)

(defn cannot-move? [board symbol]
  (let [valid-move-seq (valid-move-seq board symbol)]
       (and (= (count valid-move-seq) 1) (= (first valid-move-seq) {:from-coord [-1 -1] :to-coord [-1 -1]}))
  )
)

(defn can-move? [board symbol]
  (not (cannot-move? board symbol))
)

(defn board-to-str [board row-tabulator]
  (loop [
          i 6
	  board-str (str row-tabulator "    1 2 3 4 5 6 7 \n\n")
	]
	(if (< i 0)
	  (str board-str
	       "\n\n" row-tabulator "    1 2 3 4 5 6 7 "
	  )
	  (recur
	    (dec i)
	    (str board-str
	    	 (loop [
		         j 0
		         line-str ""
		       ]
		   (if (>= j 7)
		     (str row-tabulator (inc i) "  " line-str "|  " (inc i))
		     (recur
		       (inc j)
		       (let [symbol (game-utils-misc/lookup board [j i])]
		         (str line-str
			   (cond (not symbol) "| "
				 :other		   (str "|" symbol "")
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

(defn boards-to-str [boards row-tabulator]
  (let [
         partial-board-to-str #(board-to-str % row-tabulator)
       ]
       (vec (map partial-board-to-str boards))
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
    (has-won? board "M")
    (has-won? board "O")
    (is-full? board)
  )
)

(defn cutoff? [board ply-depth max-ply-depth]
  (or
    (terminal? board)
    (>= ply-depth max-ply-depth)
  )
)

(defn successor [board move]
  (make-move board move)
)

(defn successors [board symbol]
  (for [move (valid-move-seq board symbol)]
       [move (successor board move)]
  )
)

(defn evaluate-heuristic-connect-four [board turn]
  (let [
         conquest-analysis (fn [strip-vec]
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
	                                        (= @M-count 4) 250
	                                      )
	                                      (cond
	                                        (= @O-count 1) -1
	                                        (= @O-count 2) -10
	                                        (= @O-count 3) -50
	                                        (= @O-count 4) -250
	                                      )
	                                  )
                                      )
                                 )
                             )
                           )
         conquest-rate (fn []
                         (let [sum (atom 0)]
                              (doseq [j (range 7),
	                              i (range 7),
	                              direction (list :NE :E :SE :S)]
                                     (let [
                                            strip (lookup-strip board [j i] direction 4),
	                                    analysis-result (conquest-analysis strip)
	                                  ]
	                                  (swap! sum + analysis-result)
                                     )
                              )
                              @sum
                          )
                        )
       ]
       (let [bonus (if (= turn "M") 16 -16)] (+ (conquest-rate) bonus))
  )
)

(defn count-neighbourhood-symbols [board coord radius]
  (let [center-symbol (game-utils-misc/lookup board coord)]
       (count (filter #(= (game-utils-misc/lookup board %) center-symbol) (get-neighbourhood board coord radius)))
  )
)

(defn evaluate-heuristic-infection1 [board]
  (let [
         coords-with-symbol-M (get-all-coords-with-given-symbol board "M")
	 neighbourhood-symbol-count-M (apply + (for [coord coords-with-symbol-M] (count-neighbourhood-symbols board coord 1)))
         coords-with-symbol-O (get-all-coords-with-given-symbol board "O")
	 neighbourhood-symbol-count-O (apply + (for [coord coords-with-symbol-O] (count-neighbourhood-symbols board coord 1)))
       ]
       (- neighbourhood-symbol-count-M neighbourhood-symbol-count-O)
  )
)

(defn evaluate [board turn]
  (cond
    (has-won? board "M") 32767
    (has-won? board "O") -32767
    :other (evaluate-heuristic-infection1 board)
  )
)

(declare max-val min-val)

(defn min-val [board alpha beta ply-depth max-ply-depth]
  (if (cutoff? board ply-depth max-ply-depth)
    {:value (evaluate board "O"), :leaf-count 1}
    (let [
           v (atom (Integer/MAX_VALUE))
	   beta-local (atom beta)
	   leaf-count (atom 0)
	   keep-searching (atom true)
	 ]
         (doseq [[_ succ-board] (successors board "O")]
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
    (do
      (let [valid-moves (valid-move-seq board "M")]
        {
          :action (nth valid-moves (rand-int (count valid-moves)))
          :value (evaluate board "M")
          :leaf-count 1
        }
      )
    )
    
    (let [
           v (atom (Integer/MIN_VALUE))
	   m (atom {:from-coord [0 0] :to-coord [0 0]})
	   alpha-local (atom alpha)
	   leaf-count (atom 0)
	   keep-searching (atom true)
	 ]
         (doseq [[succ-move succ-board] (shuffle (successors board "M"))]
	   (if @keep-searching
	     (let [min-result (min-val succ-board @alpha-local beta (inc ply-depth) max-ply-depth)]
	       (if (> (:value min-result) @v)
	         (do
		   (reset! m succ-move)
		   (reset! v (:value min-result))
		 )
	       )
	       (swap! leaf-count + (:leaf-count min-result))
	       (if (>= @v beta) (reset! keep-searching false))
	       (swap! alpha-local max @v)
	     )
	   )
	 )
         {:action @m, :value @v, :leaf-count @leaf-count}
    )
  )
)

(defn alpha-beta-search [board max-ply-depth]
  (:action (max-val board (Integer/MIN_VALUE) (Integer/MAX_VALUE) 0 max-ply-depth))
)

(defn next-move [board max-ply-depth]
  (do
    (alpha-beta-search board max-ply-depth)
  )
)
