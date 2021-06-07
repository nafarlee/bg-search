(ns sql.insert
  (:require
    [clojure.set :refer [union]]
    [clojure.string :as s]
    string
    [sql.dsl :refer [clj->sql]]))

(defn generate [table columns uniques chunks]
  {:post [(or (nil? %)
              (and (contains? % :text)
                   (contains? % :values)))]}
  (when (seq chunks)
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
                :do :update :set updates))))

(defn one-to-many [property game]
  {:post [(set? %)]}
  (->> (get game property)
       (map (juxt :id :value))
       set))

(defn many-to-many [property game]
  {:post [(set? %)]}
  (->> (get game property)
       (map (juxt (constantly (:id game)) :id))
       set))

(defn many-to-many-symmetric [root leaf game]
  {:post [(set? %)]}
  (union
   (->> (get game root)
        (map (juxt (constantly (:id game)) :id))
        set)
   (->> (get game leaf)
        (map (juxt :id (constantly (:id game))))
        set)))

(defn mapset [f coll]
  {:post [(set? %)]}
  (->> coll
       (map f)
       (filter some?)
       (apply union)))

(defn games [gs]
  (let [columns     ["id"
                     "primary_name"
                     "year"
                     "last_updated"
                     "average_rating"
                     "bayes_rating"
                     "rating_votes"
                     "rating_deviation"
                     "average_weight"
                     "weight_votes"
                     "minimum_players"
                     "maximum_players"
                     "minimum_playtime"
                     "maximum_playtime"
                     "minimum_age"
                     "image"
                     "thumbnail"
                     "description"]
        game->chunk (fn [game]
                      (map #(get game (keyword (string/snake->kebab %)))
                           columns))
        chunks      (map game->chunk gs)]
    (generate "games" columns ["id"] chunks)))

(defn alternate-names [games]
  (let [columns ["id" "alternate_name"]]
    (generate "alternate_names"
              columns
              columns
              (mapset (fn [{:keys [id alternate-names]}]
                        (set (map vector (repeat id) alternate-names)))
                      games))))

(defn reimplementations [games]
  (let [columns ["original" "reimplementation"]]
    (generate "reimplementations"
              columns
              columns
              (mapset (partial many-to-many-symmetric :reimplemented-by :reimplements)
                      games))))

(defn collections [games]
  (let [columns ["item" "collection"]]
    (generate "collections"
              columns
              columns
              (mapset (partial many-to-many-symmetric :contained-in :contains)
                      games))))

(defn expansions [games]
  (let [columns ["base" "expansion"]]
    (generate "expansions"
              columns
              columns
              (mapset (partial many-to-many-symmetric :expanded-by :expands)
                      games))))

(defn publishers [games]
  (generate "publishers"
            ["id" "publisher"]
            ["id"]
            (mapset (partial one-to-many :publishers) games)))

(defn games-publishers [games]
  (let [columns ["game_id" "publisher_id"]]
    (generate "games_publishers"
              columns
              columns
              (mapset (partial many-to-many :publishers) games))))

(defn mechanics [games]
  (generate "mechanics"
            ["id" "mechanic"]
            ["id"]
            (mapset (partial one-to-many :mechanics) games)))

(defn games-mechanics [games]
  (let [columns ["game_id" "mechanic_id"]]
    (generate "games_mechanics"
              columns
              columns
              (mapset (partial many-to-many :mechanics) games))))

(defn families [games]
  (generate "families"
            ["id" "family"]
            ["id"]
            (mapset (partial one-to-many :families) games)))

(defn games-families [games]
  (let [columns ["game_id" "family_id"]]
    (generate "games_families"
              columns
              columns
              (mapset (partial many-to-many :families) games))))

(defn artists [games]
  (generate "artists"
            ["id" "artist"]
            ["id"]
            (mapset (partial one-to-many :artists) games)))

(defn games-artists [games]
  (let [columns ["game_id" "artist_id"]]
    (generate "games_artists"
              columns
              columns
              (mapset (partial many-to-many :artists) games))))

(defn categories [games]
  (generate "categories"
            ["id" "category"]
            ["id"]
            (mapset (partial one-to-many :categories) games)))

(defn games-categories [games]
  (let [columns ["game_id" "category_id"]]
    (generate "games_categories"
              columns
              columns
              (mapset (partial many-to-many :categories) games))))

(defn designers [games]
  (generate "designers"
            ["id" "designer"]
            ["id"]
            (mapset (partial one-to-many :designers) games)))

(defn games-designers [games]
  (let [columns ["game_id" "designer_id"]]
    (generate "games_designers"
              columns
              columns
              (mapset (partial many-to-many :designers) games))))

(defn player-recommendations [games]
  (let [->range     #(if (s/ends-with? % "+")
                        (str "(" (s/replace % "+" "") ",)")
                        (str "[" % "," % "]"))
        game->chunk (fn [{:strs [id community-recommended-players]}]
                      (->> (get community-recommended-players "counts")
                           (map (fn [[player-count {:strs [best recommended not-recommended]}]]
                                  [id (->range player-count) best recommended not-recommended]))
                           set))]
    (generate "player_recommendations"
              ["id" "players" "best" "recommended" "not_recommended"]
              ["id" "players"]
              (mapset game->chunk games))))

(defn insert [gs]
  (->> [games
        alternate-names
        reimplementations
        collections
        expansions
        publishers
        games-publishers
        mechanics
        games-mechanics
        families
        games-families
        artists
        games-artists
        categories
        games-categories
        designers
        games-designers
        player-recommendations]
       (map #(% gs))
       (remove nil?)))
