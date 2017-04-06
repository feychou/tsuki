(ns tsuki.bot
  (:gen-class)
  (:require [clojure.string :as s]
            [environ.core :refer [env]]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [tsuki.facebook :as fb]
            [tsuki.actions :as actions]))

(def apod-formatter (f/formatter "yyyy-MM-dd"))

(defn on-message [payload]
  (println "on-message payload:")
  (println payload)
  (let [sender-id (get-in payload [:sender :id])
        recipient-id (get-in payload [:recipient :id])
        time-of-message (get-in payload [:timestamp])
        message-text (get-in payload [:message :text])]
    (cond
      (s/includes? (s/lower-case message-text) "gimme") (actions/send-astro-pic sender-id)
      :else (actions/send-astro-emoji sender-id))))

(defn on-postback [payload]
  (println "on-postback payload:")
  (println payload)
  (println (f/unparse apod-formatter (t/minus (t/now) (t/days 1))))
  (let [sender-id (get-in payload [:sender :id])
        recipient-id (get-in payload [:recipient :id])
        time-of-message (get-in payload [:timestamp])
        postback (get-in payload [:postback :payload])
        referral (get-in payload [:postback :referral :ref])]
    (cond
      (= postback "GET_STARTED") (actions/greet sender-id)
      (= postback "TODAY_APOD") (actions/send-astro-pic sender-id)
      :else (fb/send-message sender-id (fb/text-message "Sorry, I don't know how to handle that postback")))))

(defn on-attachments [payload]
  (println "on-attachment payload:")
  (println payload)
  (let [sender-id (get-in payload [:sender :id])
        recipient-id (get-in payload [:recipient :id])
        time-of-message (get-in payload [:timestamp])
        attachments (get-in payload [:message :attachments])
        attachment (first attachments)]
    (actions/send-astro-emoji sender-id)))
