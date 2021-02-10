(ns result)

(defn ok [x]
  [:ok x])

(defn ok? [[t]]
  (= :ok t))

(defn error [e]
  [:error e])

(defn error? [[t]]
  (= :error t))

(defn from-promise [p]
  (-> p
      (.then ok)
      (.catch error)))
