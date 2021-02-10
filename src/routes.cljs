(ns routes)

(defn search [req res]
  (.set res
        "Cache-Control"
        (format "public, max-age=%s" (* 60 60 24 7)))
  (let [query     (or (.. req -query -query) "")
        order     (or (.. req -query -order) "bayes_rating")
        direction (or (.. req -query -direction) "DESC")
        offset    (or (-> req (.. -query -offset) (js/parseInt 10)) 0)]))
