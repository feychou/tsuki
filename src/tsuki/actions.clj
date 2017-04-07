(ns tsuki.actions
  (:gen-class)
  (:require [tsuki.facebook :as fb]
            [clojure.data.json :as json]
            [environ.core :refer [env]]))

(defn get-today-astro-pic []
  (json/read-str (slurp (str "https://api.nasa.gov/planetary/apod?api_key=" (env :nasa-api-key))) :key-fn keyword))

(defn get-astro-pic-by-date [date]
  (json/read-str (slurp (str "https://api.nasa.gov/planetary/apod?api_key=" (env :nasa-api-key) "&date=" date)) :key-fn keyword))

(defn send-astro-pic [user-id pic]
  (fb/send-message user-id (fb/image-message (:url pic)))
  (fb/send-message user-id (fb/text-message (:title pic))))

(defn greet [user-id]
  (fb/send-message user-id (fb/text-message "hi earthling ☾"))
  (fb/send-message user-id (fb/text-message "i am tsuki and i report space facts to you"))
  (fb/send-message user-id (fb/text-message "tap on the menu below whenever you feel like")))

(defn on-menu-pick
  ([user-id] 
    (let [response (get-today-astro-pic)]
      (send-astro-pic user-id response)))
  ([user-id date] 
    (let [today-pic (get-today-astro-pic)
          chosen-pic (get-astro-pic-by-date date)]
        (if (not= (:url today-pic) (:url chosen-pic))
          (send-astro-pic user-id chosen-pic)
          (send-astro-pic user-id chosen-pic)))))

;(defn send-astro-pic [user-id & [date]]
;  (let [response get-today-astro-pic]
;    (fb/send-message user-id (fb/image-message (:url response)))
;    (fb/send-message user-id (fb/text-message (:title response)))))

(defn send-astro-emoji [user-id]
  (let [emojis [128125 128156 127773 127770 127776 128302 128126]]
    (fb/send-message user-id (fb/text-message (format "%c" (int (rand-nth emojis)))))))