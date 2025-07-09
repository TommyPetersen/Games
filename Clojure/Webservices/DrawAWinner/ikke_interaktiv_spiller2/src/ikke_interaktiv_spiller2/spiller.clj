(ns ikke-interaktiv-spiller2.spiller
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
	    [compojure.route :as route]
	    [ring.middleware.defaults :refer :all]
	    [clojure.pprint :as pp]
	    [clojure.string :as str]
	    [clojure.data.json :as json]
	    [games.draw-a-winner.ikke-interaktiv-spiller2 :as ikke-interaktiv-spiller2]
  )
  (:gen-class)
)

(def spillet-er-initialiseret? (atom false))
(def stop-fn (atom nil))

(defn initialiser-spil [req]
  (let [resultat (ikke-interaktiv-spiller2/spiller2 {:data ["initialiserSpil"]})]
    (reset! spillet-er-initialiseret? true)
    {
      :status 200
      :headers {"Content-Type" "application/json"}
      :body (json/write-str resultat)
    }
  )
)

(defn hent-naeste-traek [req]
  (if @spillet-er-initialiseret?
    (let [
	   traekvaerdi (:traekvaerdi (:params req))
           resultat (ikke-interaktiv-spiller2/spiller2 {:data ["hentNaesteTraek" traekvaerdi]})
         ]
         {
           :status 200
           :headers {"Content-Type" "application/json"}
           :body (json/write-str resultat)
         }
    )
    {
      :status 409
      :headers {"Content-Type" "application/json"}
      :body (json/write-str {:data ["Spillet er ikke blevet initialiseret!"]})
    }
  )
)

(defn meddel-traek [req]
  (if @spillet-er-initialiseret?
    (let [
	   traekvaerdi (:traekvaerdi (:params req))
           resultat (ikke-interaktiv-spiller2/spiller2 {:data ["meddelTraek" traekvaerdi]})
         ]
         {
           :status 200
           :headers {"Content-Type" "application/json"}
           :body (json/write-str resultat)
         }
    )
    {
      :status 409
      :headers {"Content-Type" "application/json"}
      :body (json/write-str {:data ["Spillet er ikke blevet initialiseret!"]})
    }
  )
)

(defn meddel-tidsudloeb [req]
  {
    :status 200
    :headers {"Content-Type" "application/json"}
    :body (json/write-str {:data ["Aah nej, tiden er udløbet!"]})
  }
)

(defn stop-tjenesten [req]
  (@stop-fn)
)

(defroutes app-routes
  (GET "/" [] "Dette er en ikke-interaktiv spiller2 til spillet \"Traek-en-vinder\".")
  (GET "/initialiserSpil" [] initialiser-spil)
  (GET "/hentNaesteTraek" [] hent-naeste-traek)
  (GET "/meddelTraek" [] meddel-traek)
  (GET "/meddel-tidsudloeb" [] meddel-tidsudloeb)
  (GET "/stopTjenesten" [] stop-tjenesten)
  
  (route/not-found "Fejl, den anmodede side findes ikke!")
)

(defn -main
  "Tjenestens startfunktion."
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3202"))]
       (reset! stop-fn (server/run-server (wrap-defaults #'app-routes api-defaults) {:port port}))
       (println (str "Koerer '" (:ns (meta #'-main)) "' som webtjeneste paa 'http://127.0.0.1:" port "'"))
  )
)
