(ns sql.insert
  (:require
    [clojure.set :refer [union]]
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

(defn one-to-many [property game]
  (->> (get game property)
       (map (juxt #(get % "id") #(get % "value")))
       set))

(defn many-to-many [property game]
  (->> (get game property)
       (map #(vector (get game "id") (get % "id")))
       set))

(defn many-to-many-symmetric [root leaf game]
  (union
   (->> (get game root)
        (map #(vector (get game "id") (get % "id")))
        set)
   (->> (get game leaf)
        (map #(vector (get % "id") (get game "id")))
        set)))

(defn mapset [f coll]
  (->> coll
       (map f)
       (filter some?)
       (apply union)))

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

(defn reimplementations [games]
  (let [columns ["original" "reimplementation"]]
  (generate "reimplementations"
            columns
            columns
            (mapset (partial many-to-many-symmetric "reimplemented-by" "reimplements")
                    games))))

(defn collections [games]
  (let [columns ["item" "collection"]]
  (generate "collections"
            columns
            columns
            (mapset (partial many-to-many-symmetric "contained-in" "contains")
                    games))))

(defn expansions [games]
  (let [columns ["base" "expansion"]]
  (generate "expansions"
            columns
            columns
            (mapset (partial many-to-many-symmetric "expanded-by" "expands")
                    games))))

(defn publishers [games]
  (generate "publishers"
            ["id" "publisher"]
            ["id"]
            (mapset (partial one-to-many "publishers") games)))

(defn games-publishers [games]
  (let [columns ["game_id" "publisher_id"]]
    (generate "games_publishers"
              columns
              columns
              (mapset (partial many-to-many "publishers") games))))

(defn mechanics [games]
  (generate "mechanics"
            ["id" "mechanic"]
            ["id"]
            (mapset (partial one-to-many "mechanics") games)))

(defn games-mechanics [games]
  (let [columns ["game_id" "mechanic_id"]]
    (generate "games_mechanics"
              columns
              columns
              (mapset (partial many-to-many "mechanics") games))))

(defn families [games]
  (generate "families"
            ["id" "family"]
            ["id"]
            (mapset (partial one-to-many "families") games)))

(defn games-families [games]
  (let [columns ["game_id" "family_id"]]
    (generate "games_families"
              columns
              columns
              (mapset (partial many-to-many "families") games))))

(defn artists [games]
  (generate "artists"
            ["id" "artist"]
            ["id"]
            (mapset (partial one-to-many "artists") games)))

(defn games-artists [games]
  (let [columns ["game_id" "artist_id"]]
    (generate "games_artists"
              columns
              columns
              (mapset (partial many-to-many "artists") games))))

(defn categories [games]
  (generate "categories"
            ["id" "category"]
            ["id"]
            (mapset (partial one-to-many "categories") games)))

(defn games-categories [games]
  (let [columns ["game_id" "category_id"]]
    (generate "games_categories"
              columns
              columns
              (mapset (partial many-to-many "categories") games))))

(defn designers [games]
  (generate "designers"
            ["id" "designer"]
            ["id"]
            (mapset (partial one-to-many "designers") games)))

(defn games-designers [games]
  (let [columns ["game_id" "designer_id"]]
    (generate "games_designers"
              columns
              columns
              (mapset (partial many-to-many "designers") games))))
