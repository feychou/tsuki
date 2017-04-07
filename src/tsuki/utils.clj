(ns tsuki.utils
  (:gen-class)
  (:require [java-time :as t]))

(defn get-day [x]
	(t/format "yyyy-MM-dd" (t/minus (t/local-date) (t/days x))))

(def yesterday (get-day 1))

(def day-before-yesterday (get-day 2))