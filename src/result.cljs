(ns result)

(defn ok [x]
  [:ok x])

(defn error [e]
  [:error e])
