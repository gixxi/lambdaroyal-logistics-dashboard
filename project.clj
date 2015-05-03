(defproject lambdaroyal-logistics-dashboard "0.1-SNAPSHOT"
  :description "Warehouse Tower :)"
  :url "https://github.com/gixxi/lambdaroyal-logistics-dashboard"
  :license {:name "GPL v3"
            :url ""}
  :dependencies [[org.clojure/clojure "1.7.0-alpha5"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.ashafa/clutch "0.4.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.1"]
                 [org.lambdaroyal/clojure-util "1.0-SNAPSHOT"]
                 [javax.servlet/servlet-api "2.5"]
                 [ring/ring-json "0.3.1"]
                 [clj-time "0.9.0"]
                 [clj-jwt "0.0.13"]
                 [http-kit "2.1.16"]
                 [compojure "1.3.2"]
                 [log4j "1.2.15" 
                  :exclusions [javax.mail/mail
                             javax.jms/jms
                             com.sun.jdmk/jmxtools
                             com.sun.jmx/jmxri]]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}}
  :aot [])
