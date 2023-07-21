(ns games.game-utilities-misc)

(defn lookup [board [j i]]
  (try
    (nth (nth board j) i)
    (catch Exception E nil)
  )
)
