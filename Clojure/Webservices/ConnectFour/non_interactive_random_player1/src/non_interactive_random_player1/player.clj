(ns non-interactive-random-player1.player
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
	    [compojure.route :as route]
	    [ring.middleware.defaults :refer :all]
	    [clojure.pprint :as pp]
	    [clojure.string :as str]
	    [clojure.data.json :as json]
	    [games.connect-four.non-interactive-random-player :as non-interactive-random-player]
  )
  (:gen-class)
)

(def game-initialised (atom false))

(defn init-game [req]
  (let [result (non-interactive-random-player/player1 {:data ["init-game"]})]
    (reset! game-initialised true)
    {
      :status 200
      :headers {"Content-Type" "text/json"}
      :body (json/write-str result)
    }
  )
)

(defn get-first-move [req]
  (if @game-initialised
    (let [
           result (non-interactive-random-player/player1 {:data ["get-first-move"]})
         ]
         {
           :status 200
           :headers {"Content-Type" "text/json"}
           :body (json/write-str result)
         }
    )
    {
      :status 409
      :headers {"Content-Type" "text/plain"}
      :body "The game has not been initialised!"
    }
  )
)

(defn get-next-move [req]
  (if @game-initialised
    (let [
	   move-value (:move-value (:params req))
           result (non-interactive-random-player/player1 {:data ["get-next-move" move-value]})
         ]
         {
           :status 200
           :headers {"Content-Type" "text/json"}
           :body (json/write-str result)
         }
    )
    {
      :status 409
      :headers {"Content-Type" "text/plain"}
      :body "The game has not been initialised!"
    }
  )
)

(defn notify-move [req]
  (if @game-initialised
    (let [
	   move-value (:move-value (:params req))
           result (non-interactive-random-player/player1 {:data ["notify-move" move-value]})
         ]
         {
           :status 200
           :headers {"Content-Type" "text/json"}
           :body (json/write-str result)
         }
    )
    {
      :status 409
      :headers {"Content-Type" "text/plain"}
      :body "The game has not been initialised!"
    }
  )
)

(defn notify-timeout [req]
  {
    :status 200
    :headers {"Content-Type" "text/json"}
    :body (json/write-str {:data ["Oh no, time is up!"]})
  }
)

(defroutes app-routes
  (GET "/" [] "This is a non-interactive random player for the game \"Connect Four\".")
  (GET "/init-game" [] init-game)
  (GET "/get-first-move" [] get-first-move)
  (GET "/get-next-move" [] get-next-move)
  (GET "/notify-move" [] notify-move)
  (GET "/notify-timeout" [] notify-timeout)
  
  (route/not-found "Error, page not found!")
)

(defn -main
  "Application main entry."
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3003"))]
       (server/run-server (wrap-defaults #'app-routes site-defaults) {:port port})
       (println (str "Running '" (:ns (meta #'-main)) "' as webservice at 'http://127.0.0.1:" port "'"))
  )
)
