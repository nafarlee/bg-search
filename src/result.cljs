(ns result)

(defn ok [x]
  [:ok x])

(defn ok? [[t]]
  (= :ok t))

(defn error [e]
  [:error e])

(defn error? [[t]]
  (= :error t))

(def unwrap second)

(defn from-promise [p]
  (-> p
      (.then ok)
      (.catch error)))
