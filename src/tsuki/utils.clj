(ns tsuki.utils
  (:gen-class)
  (:require [java-time :as t]
            [clojure.string :as s]
            [environ.core :refer [env]]))

(def dynamo-creds {:access-key (env :dynamo-access-key)
                   :secret-key (env :dynamo-secret-key)
                   :endpoint (env :dynamo-endpoint)})

(defn get-day [x]
  (t/format "yyyy-MM-dd" (t/minus (t/local-date) (t/days x))))

(defn last-year []
  (- (read-string (first (s/split (t/format "yyyy-MM-dd" (t/local-date)) #"-"))) 1))

(def yesterday (get-day 1))

(def day-before-yesterday (get-day 2))

(defn add-nulls [num-vector]
  (map #(str 0 %) num-vector))

(defn random-date []
  (str 
    (rand-nth (range 1997 (last-year))) "-"
    (rand-nth (concat (add-nulls (range 1 9)) (range 10 12))) "-"
    (rand-nth (concat (add-nulls (range 1 9)) (range 10 28)))))