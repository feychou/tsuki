(ns tsuki.actions
  (:gen-class)
  (:require [tsuki.facebook :as fb]
            [clojure.data.json :as json]
            [environ.core :refer [env]]))

(defn greet [user-id]
  (println "Greeting: ")
  (println user-id)
  (fb/send-message user-id (fb/quick-replies-message "Send me your location please" [{"content_type" "location"}])))

(defn send-directions [user-id coordinates]
  (fb/send-message user-id (fb/text-message "Here are your directions"))
  (fb/send-message user-id (fb/text-message (str "https://google.com/maps/dir/" (:lat coordinates) "," (:long coordinates) "/48.190870,16.318560"))))

(defn send-astro-pic [user-id]
  (let [response (json/read-str (slurp (str "https://api.nasa.gov/planetary/apod?api_key=" (env :nasa-api-key))) :key-fn keyword)]
    (fb/send-message user-id (fb/image-message (:url response)))))

(defn send-astro-emoji [user-id]
  (let [emojis [128125 128156 127773 127770 127776 128302 128126]]
    (fb/send-message user-id (fb/text-message (format "%c" (int (rand-nth emojis)))))))