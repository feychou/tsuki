(ns tsuki.jobs
	(:gen-class)
  (:require [tsuki.actions :as actions]
            [tsuki.utils :as utils]
            [chime :refer [chime-at]]
            [clj-time.core :as t]
            [clj-time.periodic :refer [periodic-seq]]
            [taoensso.faraday :as far])
  (:import [org.joda.time DateTimeZone]))

(def interval 
  (->> (periodic-seq (.. (t/now)
                         (withZone (DateTimeZone/forID "Europe/Berlin"))
                         (withTime 09 0 0 0))
                     (-> 1 t/days))))

(defn send-apod-to-subscribers []
  (chime-at interval
            (fn [time]
              (println "Chiming at " time)
              (println (str "All ids " (far/scan utils/dynamo-creds :tsuki-users)))
              (doseq [subscriber (far/scan utils/dynamo-creds :tsuki-users)]
                     (actions/send-astropic-template (:fb-id subscriber) (actions/get-today-astro-pic) :toast true :menu false)))))
