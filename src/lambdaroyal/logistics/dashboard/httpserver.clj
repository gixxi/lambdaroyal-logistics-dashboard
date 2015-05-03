(ns lambdaroyal.logistics.dashboard.httpserver
  (require [compojure.core :refer [defroutes routes GET POST DELETE ANY context]]
           [compojure.route :as route]
           [compojure.handler :as handler]
           [org.httpkit.server :refer :all]
           [ring.middleware.json :as middleware])
  (:gen-class))

(defn contrib-start [system]
  (let [_ (println :routes (:routes system))
        r (apply routes
                 (conj
                  (vec (:routes system))
                  (route/files "/static/" {:root "public/app"}) ;; static file url prefix /static, in `public` folder
                  (route/not-found "<p>Page not found.</p>")))
        r (-> r (middleware/wrap-json-body) (middleware/wrap-json-response))]
    (assoc system :httpserver (run-server (handler/site r) {:port (or (-> system :args :port) 8080)}))))

(defn contrib-stop [system]
  (do
    (if-let [server (:httpserver system)]
      (do
        (println "stopping HTTP server ...")
        (server :timeout 2500)))
    (dissoc system :httpserver)))
