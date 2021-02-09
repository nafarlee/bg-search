(ns static
  (:require
    [shadow.resource :as rc]))

(def credentials (js/JSON.parse (rc/inline "/db-credentials.json")))
