(ns facebook-example.actions
  (:gen-class)
  (:require [facebook-example.facebook :as fb]))

(defn greet [user-id]
  (println "Greeting: ")
  (println user-id)
  (fb/send-message user-id (fb/quick-replies-message "Send me your location please" [{"content_type" "location"}])))

(defn send-directions [user-id coordinates]
  (fb/send-message user-id (fb/text-message "Here are your directions"))
  (fb/send-message user-id (fb/text-message (str "https://google.com/maps/dir/" (:lat coordinates) "," (:long coordinates) "/48.190870,16.318560"))))
