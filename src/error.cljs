(ns error
  (:require
    [view :as v]))

(defn transpile [error res query]
  (let [code       422
        message    (str "Your search has an error! Hopefully the hint below"
                        " will help you find it.")
        offset     (or (.. error -result -index -offset) 0)
        padding    (apply str (repeat offset " "))
        annotation (str padding "^ This is the first 'bad' character")
        block      (str query "\n" annotation)]
    (-> res
        (.status code)
        (.send (v/error {:code    code
                         :message message
                         :block   block})))))

(defn generic [error res code]
  (js/console.error error)
  (-> res
      (.status code)
      (.send (v/error {:code    code
                       :message error}))))
