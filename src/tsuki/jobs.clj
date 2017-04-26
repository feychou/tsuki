(ns tsuki.jobs
	(:gen-class)
  (:require [tsuki.actions :as actions]
            [tsuki.utils :as utils]
            [chime :refer [chime-at]]
            [clj-time.core :as t]
            [clj-time.periodic :refer [periodic-seq]]
            [taoensso.faraday :as far]))

(def interval 
  (rest
   (periodic-seq (t/now)
                 (-> 30 t/seconds))))

(defn send-apod-to-subscribers []
  (chime-at interval
            (fn [time]
              (println "Chiming at " time)
              (println (str "All ids " (far/scan utils/dynamo-creds :tsuki-users)))
              (doseq [subscriber (far/scan utils/dynamo-creds :tsuki-users)]
                      (actions/send-astro-pic (:fb-id subscriber) (actions/get-today-astro-pic))))))
