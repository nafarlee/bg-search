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

(def all
  #js{:percentageOf percentage-of})
