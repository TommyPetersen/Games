(ns arbiters.non-interactive-arbiter
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
	    [compojure.route :as route]
	    [ring.middleware.defaults :refer :all]
	    [clojure.pprint :as pp]
	    [clojure.string :as str]
	    [clojure.data.json :as json]
	    [games.connect-four.non-interactive-arbiter :as non-interactive-arbiter]
  )
  (:gen-class)
)

(def game-initialised (atom false))

(defn init-game [req]
  (let [result (non-interactive-arbiter/arbiter {:data ["initGame"]})]
    (reset! game-initialised true)
    {
      :status 200
      :headers {"Content-Type" "text/html"}
      :body (json/write-str result)
    }
  )
)

(defn new-move [req]
  (do
    (pp/pprint req)
  )
  (if @game-initialised
    (let [
  	   player (:player (:params req))
	   move-value (:move-value (:params req))
           result (non-interactive-arbiter/arbiter {:data ["newMove" player move-value]})
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

(defn get-status [req]
  (let [result (non-interactive-arbiter/arbiter {:data ["getStatus"]})]
    {
      :status 200
      :headers {"Content-Type" "text/html"}
      :body (json/write-str result)
    }
  )
)

(defroutes app-routes
  (GET "/" [] "This is a non-interactive arbiter for the game \"Connect Four\".")
  (GET "/init-game" [] init-game)
  (GET "/new-move" [] new-move)
  (GET "/get-status" [] get-status)
  (route/not-found "Error, page not found!")
)

(defn -main
  "Application main entry."
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
       (server/run-server (wrap-defaults #'app-routes site-defaults) {:port port})
       (println (str "Running arbiter webserver at http://127.0.0.1:" port "/"))
  )
)
