(ns views.locals)

(defn- percentage-of
  ([numer denom]
   (percentage-of numer denom 1))
  ([numer denom decimals]
   (-> numer
       (/ denom)
       (* 100)
       (.toFixed decimals)
       (str "%"))))

(defn- range->text [r]
  (let [regex         #"\[(\d+),(\d*)\)"
        [_ start end] (re-matches regex r)]
    (str start (when (empty? end) "+"))))

(def all
  #js{:rangeToText  range->text
      :percentageOf percentage-of})
