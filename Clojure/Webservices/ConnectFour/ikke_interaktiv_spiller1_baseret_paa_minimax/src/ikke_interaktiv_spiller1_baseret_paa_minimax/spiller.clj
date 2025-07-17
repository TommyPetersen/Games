(ns ikke-interaktiv-spiller1-baseret-paa-minimax.spiller
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
	    [compojure.route :as route]
	    [ring.middleware.defaults :refer :all]
	    [clojure.pprint :as pp]
	    [clojure.string :as str]
	    [clojure.data.json :as json]
	    [games.connect-four.ikke-interaktiv-spiller-baseret-paa-minimax :as ikke-interaktiv-spiller-baseret-paa-minimax]
  )
  (:gen-class)
)

(def spillet-er-initialiseret? (atom false))
(def stop-fn (atom nil))

(defn initialiser-spil [req]
  (let [resultat (ikke-interaktiv-spiller-baseret-paa-minimax/spiller1 {:data ["initialiserSpil"]})]
    (reset! spillet-er-initialiseret? true)
    {
      :status 200
      :headers {"Content-Type" "application/json"}
      :body (json/write-str resultat)
    }
  )
)

(defn hent-foerste-traek [req]
  (if @spillet-er-initialiseret?
    (let [
           resultat (ikke-interaktiv-spiller-baseret-paa-minimax/spiller1 {:data ["hentFoersteTraek"]})
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

(defn hent-naeste-traek [req]
  (if @spillet-er-initialiseret?
    (let [
	   traekvaerdi (:traekvaerdi (:params req))
           resultat (ikke-interaktiv-spiller-baseret-paa-minimax/spiller1 {:data ["hentNaesteTraek" traekvaerdi]})
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
           resultat (ikke-interaktiv-spiller-baseret-paa-minimax/spiller1 {:data ["meddelTraek" traekvaerdi]})
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
  (GET "/" [] "Dette er en ikke-interaktiv spiller1 til spillet \"Fire-på-stribe\".")
  (GET "/initialiserSpil" [] initialiser-spil)
  (GET "/hentFoersteTraek" [] hent-foerste-traek)
  (GET "/hentNaesteTraek" [] hent-naeste-traek)
  (GET "/meddelTraek" [] meddel-traek)
  (GET "/meddelTidsudloeb" [] meddel-tidsudloeb)
  (GET "/stopTjenesten" [] stop-tjenesten)
  
  (route/not-found "Fejl, den anmodede side findes ikke!")
)

(defn -main
  "Tjenestens startfunktion."
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3001"))]
       (reset! stop-fn (server/run-server (wrap-defaults #'app-routes api-defaults) {:port port}))
       (println (str "Koerer '" (:ns (meta #'-main)) "' som webtjeneste paa 'http://127.0.0.1:" port "'"))
  )
)
