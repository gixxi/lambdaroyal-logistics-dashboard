(ns lambdaroyal.logistics.dashboard.dispatch
  (require [clojure.core.async :refer [>! <! alts! timeout chan go]]
           [lambdaroyal.logistics.dashboard.httpserver]
           [compojure.core :refer [defroutes routes GET POST DELETE ANY context]]
           [org.httpkit.server :refer :all]
           [cheshire.core :as json]
           [clojure.tools.logging :as log])
  (:gen-class))

;; --------------------------------------------------------------------
;; DOMAIN LOGIC
;; --------------------------------------------------------------------

;; --------------------------------------------------------------------
;; END - DOMAIN LOGIC
;; --------------------------------------------------------------------

;; --------------------------------------------------------------------
;; GLUE
;; --------------------------------------------------------------------

(defn dispatch
  [system event]
  (doseq [channel @(:clients system)]
    (send! (key channel) (json/generate-string {:event "event" :data event}))))

(defn ws-dispatch
  "we use event driven non-blocking I/O for requesting searches and providing back search results for cards. We use web sockets that can easily degenerate to long-pooling"
  [system]
  (fn [request]
    (with-channel request channel
      (log/info (str "client " channel " connected."))
      (swap! (:clients system) assoc channel true)
      (on-close channel (fn [status]
                          (swap! (:clients system) dissoc channel)
                          (log/info (str "client " channel " disconnected. status " status)))))))

(defn ws-config [system]
  (fn [request]
    {:status 200 :headers {"Content-Type" "application/json"} :body @(:config system)}))

(defn send-random-event [system]
  (let [end (System/currentTimeMillis)
        delay (rand-int 10000)
        start (- end delay)
        taskName (-> system :config deref rand-nth)
        most-recent (get (-> system :most-recent deref) taskName)
        start (if most-recent (max most-recent start) start)
        _ (swap! (:most-recent system) assoc taskName end)]
    (dispatch system
              {"startDate" start
               "endDate" end
               "taskName" taskName})))

(defn task-random-event [system]
  (future
    (loop []
      (if (-> system :stopped deref) nil)
      (send-random-event system)
      (Thread/sleep (rand-int 5000))
      (recur))))


;; --------------------------------------------------------------------
;; END - GLUE
;; --------------------------------------------------------------------


;; --------------------------------------------------------------------
;; SYSTEM CONTRIBUTOR CONTRACT
;; Add the following attributes to the system
;; cardSearchClients - (atom {}) denotes the channels to clients
;; that have open websocket connection for fetching event data
;; --------------------------------------------------------------------

(defn contrib-start [system]
  (let [r [(GET "/config" [] (ws-config system))
           (GET "/event" [] (ws-dispatch system))]]
    (assoc 
      system
      :most-recent (atom {})
      :routes (apply conj (:routes system) r))))

(defn contrib-stop [system]
  system)

;; --------------------------------------------------------------------
;; END - SYSTEM CONTRIBUTOR CONTRACT
;; --------------------------------------------------------------------










