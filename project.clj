(defproject tsuki "0.1.0-SNAPSHOT"
  :description "Facebook Messenger Bot in Clojure that sends astronomic pics of the day"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.1"]
                 [org.clojure/core.async "0.3.442"]
                 [compojure "1.5.1"]
                 [http-kit "2.2.0"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [environ "1.1.0"]
                 [clojure.java-time "0.2.2"]
                 [com.taoensso/faraday "1.9.0"]]
  :min-lein-version "2.0.0"
  :plugins [[lein-ring "0.9.7"]
            [lein-environ "1.1.0"]]
  ;:hooks [environ.leiningen.hooks]
  :ring {:handler tsuki.core/app}
  :uberjar-name "tsuki-standalone.jar")
  ; :profiles {:default [:base :dev :user]
  ;            #_:production #_{:env {:production false}}})
