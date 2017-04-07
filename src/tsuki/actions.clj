(ns tsuki.actions
  (:gen-class)
  (:require [tsuki.facebook :as fb]
            [tsuki.utils :as utils]
            [clojure.data.json :as json]
            [environ.core :refer [env]]))

(defn get-astro-pic [date]
  (json/read-str (slurp (str "https://api.nasa.gov/planetary/apod?api_key=" (env :nasa-api-key) "&date=" date)) :key-fn keyword))

(defn get-today-astro-pic []
  (get-astro-pic nil))

(defn make-adder [x]
  (let [y x]
    (fn [z] (+ y z))))
(def add2 (make-adder 2))
(add2 4)

(defn send-astro-pic [user-id pic]
  (fb/send-message user-id (fb/image-message (:url pic)))
  (fb/send-message user-id (fb/text-message (:title pic))))

(defn greet [user-id]
  (fb/send-message user-id (fb/text-message "hi earthling ☾"))
  (fb/send-message user-id (fb/text-message "i am tsuki and i report space facts to you"))
  (fb/send-message user-id (fb/text-message "tap on the menu below whenever you feel like")))

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