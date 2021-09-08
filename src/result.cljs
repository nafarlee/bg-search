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

(defn attempt [f & args]
  (try
    (ok (apply f args))
    (catch :default e (error e))))

(defn ->js-promise [r]
  (if (ok? r)
    (js/Promise.resolve (unwrap r))
    (js/Promise.reject (unwrap r))))
