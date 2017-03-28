(ns tsuki.actions
  (:gen-class)
  (:require [tsuki.facebook :as fb]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [environ.core :refer [env]]))

(defn greet [user-id]
  (println "Greeting: ")
  (println user-id)
  (fb/send-message user-id (fb/quick-replies-message "Send me your location please" [{"content_type" "location"}])))

(defn send-directions [user-id coordinates]
  (fb/send-message user-id (fb/text-message "Here are your directions"))
  (fb/send-message user-id (fb/text-message (str "https://google.com/maps/dir/" (:lat coordinates) "," (:long coordinates) "/48.190870,16.318560"))))

(defn give-astro-pic [user-id]
  (let [response (json/read-str (slurp (str "https://api.nasa.gov/planetary/apod?api_key=" (env :nasa-api-key))) :key-fn keyword)]
    (fb/send-message user-id (fb/image-message (:url response)))))