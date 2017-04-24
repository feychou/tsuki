(ns tsuki.bot
  (:gen-class)
  (:require [clojure.string :as s]
            [environ.core :refer [env]]
            [tsuki.facebook :as fb]
            [tsuki.actions :as actions]
            [tsuki.utils :as utils]))

(defn on-message [payload]
  (println "on-message payload:")
  (println payload)
  (actions/send-astro-emoji (get-in payload [:sender :id])))

(defn on-postback [payload]
  (println "on-postback payload:")
  (println payload)
  (let [sender-id (get-in payload [:sender :id])
        recipient-id (get-in payload [:recipient :id])
        time-of-message (get-in payload [:timestamp])
        postback (get-in payload [:postback :payload])
        referral (get-in payload [:postback :referral :ref])]
    (cond
      (= postback "GET_STARTED") (actions/greet sender-id)
      (= postback "TODAY_APOD") (actions/on-menu-pick sender-id)
      (= postback "YESTERDAY_APOD") (actions/on-menu-pick sender-id utils/yesterday postback)
      (= postback "RANDOM_APOD") (actions/on-menu-pick sender-id (utils/random-date))
      (= postback "MANAGE_SUBSCRIPTION") (actions/on-manage-subscription sender-id)
      :else (fb/send-message sender-id (fb/text-message "Sorry, I don't know how to handle that postback")))))

(defn on-attachments [payload]
  (println "on-attachment payload:")
  (println payload)
  (actions/send-astro-emoji (get-in payload [:sender :id])))

(defn on-quickreply [payload]
  (println "on-quickreply payload:")
  (println payload)
  (let [sender-id (get-in payload [:sender :id])
        recipient-id (get-in payload [:recipient :id])
        time-of-message (get-in payload [:timestamp])
        message (get-in payload [:message])
        quick-reply (get-in payload [:message :quick_reply])
        quick-reply-payload (get-in payload [:message :quick_reply :payload])]
    (cond
      (= quick-reply-payload "NO_SUBSCRIPTION") (actions/demotivate sender-id)
      (= quick-reply-payload "NO_UNSUBSCRIPTION") (actions/motivate sender-id)
      (= quick-reply-payload "SUBSCRIBE") (actions/subscribe sender-id)
      (= quick-reply-payload "UNSUBSCRIBE") (actions/unsubscribe sender-id)
      :else (fb/send-message sender-id (fb/text-message "Sorry, I don't know how to handle that quick reply.")))))

