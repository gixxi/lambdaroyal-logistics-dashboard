(ns lambdaroyal.logistics.dashboard.system
  (require
   [lambdaroyal.logistics.dashboard.dispatch]
   [lambdaroyal.logistics.dashboard.httpserver])
  (import
   [org.apache.log4j BasicConfigurator]
   [java.text SimpleDateFormat]
   [java.util Date])
  (:gen-class))

(defn contrib-start [system]
  system)

(defn contrib-stop [system]
  system)

(def contrib-namespaces ['lambdaroyal.logistics.dashboard.dispatch
                         'lambdaroyal.logistics.dashboard.httpserver
                         *ns*])

(defn system
  "returns a new instance of the whole application and starts it. Contributors can use the atom after taking reference of it during contrib-start."
  [& args]
  (let [args (apply hash-map args)]
    {:args {}
     :stopped (atom false)
     :routes [] 
     :middleware-handler (fn [handler] (fn [request] (handler request)))
     :clients (atom {})
     :namespaces contrib-namespaces
     :config (atom [:kommi :reserve :hrl :hrl-kt :HOFA-WA :prod])}))

(defn start
  "starts the system. performs the necessary side effects."
  [system]
  (reduce
   (fn [acc ns]
     (let [_ (println "start ns " ns)]
       ((ns-resolve ns 'contrib-start) acc)))
   system (:namespaces system)))

(defn stop 
  "stops the entire system denoted by [system]."
  [system]
  (do
    (reset! (system :stopped) true)
    (reduce
     (fn [acc ns]
       (let [_ (println "stop ns " ns)]
         ((ns-resolve ns 'contrib-stop) system)))
     system (remove #(contains? (:exlude-namespaces system) %) (:namespaces system)))))










