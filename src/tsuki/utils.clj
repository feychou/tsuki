(ns tsuki.utils
  (:gen-class)
  (:require [java-time :as t]))

(def yesterday (t/format "yyyy-MM-dd" (t/minus (t/local-date) (t/days 1))))