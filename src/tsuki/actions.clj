(ns tsuki.actions
  (:gen-class)
  (:require [tsuki.facebook :as fb]
            [tsuki.utils :as utils]
            [taoensso.faraday :as far]
            [clojure.data.json :as json]
            [clojure.string :as s]
            [clojure.core.async
             :as a
             :refer [<! go timeout]]
            [environ.core :refer [env]]))

(defn get-astro-pic [date]
  (json/read-str
    (slurp (str "https://api.nasa.gov/planetary/apod?api_key=" (env :nasa-api-key) "&date=" date))
    :key-fn keyword))

(defn get-today-astro-pic []
  (get-astro-pic nil))

(defn get-chunks [chunk]
  (re-seq #"[^.!?;]+[.!?;]?" chunk))

(defn send-astro-pic [user-id pic]
  (fb/send-message user-id (fb/image-message (:hdurl pic)))
  (go
    (fb/send-message user-id (fb/text-message (:title pic)))
    (fb/type-on user-id)
    (<! (timeout 2000))
    (let [pic-date (s/split (:date pic) #"-")]
      (fb/send-message user-id 
        (fb/button-template (first (get-chunks (:explanation pic))) [{:title "Read more"
                                                                       :type "web_url"
                                                                       :url (str "https://apod.nasa.gov/apod/ap" (subs (first pic-date) 2 4) (second pic-date) (nth pic-date 2) ".html")}])))
    (fb/type-on user-id)
    (<! (timeout 1000))
    (fb/send-message user-id 
      (fb/button-template "What do you want me to do next?" [{:title "Send today's APOD"
                                                              :type "postback"
                                                              :payload "TODAY_APOD"}
                                                             {:title "Send yda's APOD"
                                                              :type "postback"
                                                              :payload "YESTERDAY_APOD"}
                                                             {:title "Send random APOD"
                                                              :type "postback"
                                                              :payload "RANDOM_APOD"}]))))

(defn send-astro-emoji [user-id]
  (let [emojis [128125 128156 127773 127770 127776 128302 128126]]
    (fb/send-message user-id (fb/text-message (format "%c" (int (rand-nth emojis)))))))

(defn save-subscriber [user-id]
  (far/put-item utils/dynamo-creds
    :tsuki-users {:fb-id user-id}))

(defn get-subscriber [user-id]
  (far/get-item utils/dynamo-creds :tsuki-users {:fb-id user-id}))

(defn greet [user-id]
  (go
    (fb/send-message user-id (fb/text-message "Hi earthling ☾"))
    (fb/type-on user-id)
    (<! (timeout 2000))
    (fb/send-message user-id (fb/text-message "I am Tsuki and I report space facts to you."))
    (fb/type-on user-id)
    (<! (timeout 2000))
    (fb/send-message user-id (fb/text-message "Tap on the menu icon below whenever you feel like it."))))

(defn on-menu-pick
  ([user-id] (send-astro-pic user-id (get-today-astro-pic)))
  ([user-id date] (send-astro-pic user-id (get-astro-pic date)))
  ([user-id date postback] 
    (let [today-pic (get-today-astro-pic)
          chosen-pic (get-astro-pic date)]
        (if (not= (:url today-pic) (:url chosen-pic))
          (send-astro-pic user-id chosen-pic)
          (send-astro-pic user-id (get-astro-pic utils/day-before-yesterday))))))

(defn on-manage-subscription [user-id]
  (if (nil? (get-subscriber user-id))
    (do
      (fb/send-message user-id (fb/text-message "If you subscribe I'll make sure to send you a new astropic every day."))
      (fb/send-message user-id (fb/quick-replies-message "Do you want to subscribe?"
                                                         [{:content_type "text" :title "Yes" :payload "SUBSCRIBE"}
                                                          {:content_type "text" :title "No" :payload "NO_SUBSCRIPTION"}])))
    (do
      (fb/send-message user-id (fb/text-message "Do you wish to unsubscribe?")))))
  
  