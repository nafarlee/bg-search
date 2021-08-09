(ns promise)

(defn then-not [p on-failed on-resolved]
  (.then p on-resolved on-failed))

(defn js-promise? [x]
  {:post [(boolean? %)]}
  (and (some? x)
       (= js/Function
          (type (.-then x)))))

(defn wait [ms]
  (js/Promise.
    (fn [fulfill]
      (js/setTimeout fulfill ms))))
