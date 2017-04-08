(ns tsuki.actions
  (:gen-class)
  (:require [tsuki.facebook :as fb]
            [tsuki.utils :as utils]
            [clojure.data.json :as json]
            [clojure.string :as s]
            [clojure.core.async
             :as a
             :refer [<! go timeout]]
            [environ.core :refer [env]]))

(defn get-astro-pic [date]
  (json/read-str (slurp (str "https://api.nasa.gov/planetary/apod?api_key=" (env :nasa-api-key) "&date=" date)) :key-fn keyword))

(defn get-today-astro-pic []
  (get-astro-pic nil))

(defn get-chunks [chunk]
  (re-seq #"[^.!?;]+[.!?;]?" chunk))

(defn send-astro-pic [user-id pic]
  (fb/send-message user-id (fb/image-message (:hdurl pic)))
  (fb/send-message user-id (fb/text-message (:title pic)))
  (go
    (fb/type-on user-id)
    (doseq [text (get-chunks (:explanation pic))]
      (fb/type-on user-id)
      (<! (timeout 2000))
      (fb/send-message user-id (fb/text-message text)))))

(defn greet [user-id]
  (go
    (fb/send-message user-id (fb/text-message "hi earthling ☾"))
    (fb/type-on user-id)
    (<! (timeout 2000))
    (fb/send-message user-id (fb/text-message "i am tsuki and i report space facts to you"))
    (fb/type-on user-id)
    (<! (timeout 2000))
    (fb/send-message user-id (fb/text-message "tap on the menu below whenever you feel like it"))))

(defn on-menu-pick
  ([user-id] (send-astro-pic user-id (get-today-astro-pic)))
  ([user-id date] (send-astro-pic user-id (get-astro-pic date)))
  ([user-id date postback] 
    (let [today-pic (get-today-astro-pic)
          chosen-pic (get-astro-pic date)]
        (if (not= (:url today-pic) (:url chosen-pic))
          (send-astro-pic user-id chosen-pic)
          (send-astro-pic user-id (get-astro-pic utils/day-before-yesterday))))))

(defn send-astro-emoji [user-id]
  (let [emojis [128125 128156 127773 127770 127776 128302 128126]]
    (fb/send-message user-id (fb/text-message (format "%c" (int (rand-nth emojis)))))))