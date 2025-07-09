(ns ikke-interaktiv-dommer.dommer
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
	    [compojure.route :as route]
	    [ring.middleware.defaults :refer :all]
	    [clojure.pprint :as pp]
	    [clojure.string :as str]
	    [clojure.data.json :as json]
	    [games.infection.ikke-interaktiv-dommer :as ikke-interaktiv-dommer]
  )
  (:gen-class)
)

(def spillet-er-initialiseret? (atom false))
(def stop-fn (atom nil))

(defn initialiser-spil [req]
  (let [resultat (ikke-interaktiv-dommer/dommer {:data ["initialiserSpil"]})]
    (reset! spillet-er-initialiseret? true)
    {
      :status 200
      :headers {"Content-Type" "application/json"}
      :body (json/write-str resultat)
    }
  )
)

(defn nyt-traek [req]
  (if @spillet-er-initialiseret?
    (let [
  	   spiller (:spiller (:params req))
	   traekvaerdi (:traekvaerdi (:params req))
	   resultat (ikke-interaktiv-dommer/dommer {:data ["nytTraek" spiller traekvaerdi]})
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

(defn hent-status [req]
  (let [resultat (ikke-interaktiv-dommer/dommer {:data ["hentStatus"]})]
    {
      :status 200
      :headers {"Content-Type" "application/json"}
      :body (json/write-str resultat)
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
  (GET "/" [] "Dette er en ikke-interaktiv dommer til spillet \"Infektion\".")
  (GET "/initialiserSpil" [] initialiser-spil)
  (GET "/nytTraek" [] nyt-traek)
  (GET "/hentStatus" [] hent-status)
  (GET "/meddel-tidsudloeb" [] meddel-tidsudloeb)
  (GET "/stopTjenesten" [] stop-tjenesten)
  
  (route/not-found "Fejl, den anmodede side findes ikke!")
)

(defn -main
  "Tjenestens startfunktion."
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3100"))]
       (reset! stop-fn (server/run-server (wrap-defaults #'app-routes api-defaults) {:port port}))
       (println (str "Koerer '" (:ns (meta #'-main)) "' som webtjeneste paa 'http://127.0.0.1:" port "'"))
  )
)
