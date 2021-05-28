(ns sql.insert
  (:require
    string
    [sql :refer [clj->sql]]))

(defn generate [table columns uniques chunks]
  (let [chunk->row #(as-> % $
                          (map hash-set $)
                          (interpose "," $)
                          (concat "(" $ ")"))
        values     (->> chunks
                        (map chunk->row)
                        (interpose ",")
                        flatten
                        (apply clj->sql))
        updates    (->> columns
                        (map #(vector % := (str "EXCLUDED." %)))
                        (interpose ",")
                        flatten
                        (apply clj->sql))]
    (clj->sql :insert :into table (list columns)
              :values values
              :on :conflict (list uniques)
              :do :update :set updates)))

(defn games [gs]
  (let [columns     [:id
                     :image
                     :thumbnail
                     :average_rating
                     :bayes_rating
                     :description
                     :maximum_players
                     :maximum_playtime
                     :minimum_age
                     :minimum_players
                     :minimum_playtime
                     :primary_name
                     :rating_deviation
                     :rating_votes
                     :weight_votes
                     :year]
        game->chunk (fn [game]
                      (map #(get game (-> % name string/snake->kebab))
                           columns))
        chunks      (map game->chunk (js->clj gs))]
    (generate :games columns [:id] chunks)))

(defn one-to-many [games property]
  (->> games
       js->clj
       (mapcat #(get % property))
       (filter some?)
       (map (juxt #(get % "id") #(get % "value")))
       (apply hash-set)
       clj->js))

(defn publishers [games]
  (generate :publishers
            [:id :publisher]
            [:id]
            (one-to-many games "publishers")))

(defn mechanics [games]
  (generate :mechanics
            [:id :mechanic]
            [:id]
            (one-to-many games "mechanics")))
