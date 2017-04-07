(ns tsuki.utils
  (:gen-class)
  (:require [java-time :as t]))

(def yesterday (t/minus (t/local-date) (t/days 1)))