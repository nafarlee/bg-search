(ns views.locals)

(defn percentage-of
  ([numer denom]
   (percentage-of numer denom 1))
  ([numer denom decimals]
   (-> numer
       (/ denom)
       (* 100)
       (.toFixed decimals)
       (str "%"))))

(defn range->text [r]
  (let [regex         #"\[(\d+),(\d*)\)"
        [_ start end] (re-matches regex r)]
    (str start (when (empty? end) "+"))))

(defn sort-recommendations [recommendations]
  (clj->js (sort-by #(-> % (. -players) range->text (js/parseInt 10))
                    recommendations)))

(def all
  #js{:sortRecommendations sort-recommendations
      :rangeToText         range->text
      :percentageOf        percentage-of})
