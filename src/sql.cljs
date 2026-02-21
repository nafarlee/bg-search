(ns sql
  (:require
    ["pg" :refer [Pool]]
    [sql.dsl :refer [realize-query]]
    [sql.insert :refer [generate]]))

(defn pool []
  (Pool.))

(defn client [pool]
  (.connect pool))

(defn release [client]
  (.release client))

(defn query [database q]
  (->> q
       realize-query
       clj->js
       (.query database)))

(def ^:private get-game-sql
  "SELECT
    (SELECT JSON_OBJECT_AGG(players, JSON_BUILD_OBJECT('median', median, 'count', count))
     FROM play_medians
     WHERE game_id = $1) as median_playtimes,
    (SELECT ARRAY_AGG(mechanic)
      FROM mechanics
      INNER JOIN games_mechanics ON id = mechanic_id
      WHERE game_id = $1) AS mechanics,
    (SELECT ARRAY_AGG(category)
      FROM categories
      INNER JOIN games_categories ON id = category_id
      WHERE game_id = $1) AS categories,
    (SELECT ARRAY_AGG(family)
      FROM families
      INNER JOIN games_families ON id = family_id
      WHERE game_id = $1) AS families,
    (SELECT ARRAY_AGG(publisher)
      FROM publishers
      INNER JOIN games_publishers ON id = publisher_id
      WHERE game_id = $1) AS publishers,
    (SELECT ARRAY_AGG(artist)
      FROM artists
      INNER JOIN games_artists ON id = artist_id
      WHERE game_id = $1) AS artists,
    (SELECT ARRAY_AGG(designer)
      FROM designers
      INNER JOIN games_designers ON id = designer_id
      WHERE game_id = $1) AS designers,
    (SELECT ARRAY_AGG(alternate_name)
      FROM alternate_names
      WHERE id = $1) AS alternate_names,
    (SELECT JSON_AGG(player_recommendations)
      FROM player_recommendations
      WHERE id = $1) AS player_recommendations,
    games.id,
    last_updated,
    image,
    language_dependence,
    average_rating,
    average_weight,
    bayes_rating,
    steamdb_rating,
    description,
    maximum_players,
    maximum_playtime,
    minimum_age,
    minimum_players,
    minimum_playtime,
    primary_name,
    rating_deviation,
    rating_votes,
    weight_votes,
    year
  FROM games
  WHERE games.id = $1
  GROUP BY games.id")

(defn get-game [database id]
  (-> database
      (.query get-game-sql #js[id])
      (.then #(-> % .-rows first))))

(defn play? [database id]
  (-> database
      (.query "SELECT id FROM plays WHERE id = $1 LIMIT 1" #js[id])
      (.then #(some-> % .-rows first .-id))))

(defn game? [database id]
  (-> database
      (.query "SELECT id FROM games WHERE id = $1 LIMIT 1" #js[id])
      (.then #(some-> % .-rows first .-id))))

(defn get-last-game [database]
  (-> database
      (.query "SELECT id FROM games ORDER BY id DESC LIMIT 1")
      (.then #(-> % .-rows first .-id))))

(defn get-game-checkpoint [database]
  (-> database
      (.query "SELECT count FROM globals")
      (.then #(-> % .-rows first .-count))))

(defn get-plays-checkpoint [database]
  (-> database
      (.query "SELECT play_id, play_page FROM globals")
      (.then (fn [result]
               (-> result
                   .-rows
                   first
                   ((juxt
                     (fn [^js o] (.-play_id o))
                     (fn [^js o] (.-play_page o)))))))))

(defn begin [database]
  (.query database "BEGIN"))

(defn commit [database]
  (.query database "COMMIT"))

(defn rollback [database]
  (.query database "ROLLBACK"))

(defn update-game-checkpoint [database checkpoint]
  (.query database "UPDATE globals SET count = $1 WHERE id = $2" #js[checkpoint 1]))

(defn get-game-id-cliff [database]
  (-> database
      (.query "SELECT value FROM kv WHERE key = 'game-id-cliff'")
      (.then #(-> % .-rows first .-value))))

(defn mobius-games [database]
  (update-game-checkpoint database 1))

(defn update-plays-checkpoint [database play-id play-page]
  (.query database
          "UPDATE globals SET play_id=$1, play_page=$2 WHERE id=1"
          #js[play-id play-page]))

(defn- play->row [{:keys [id game-id length players]}]
  [id game-id length (count players)])

(defn save-plays [db-pool play-id play-page plays]
  (.then
   (client db-pool)
   (fn [db-client]
    (-> (begin db-client)
        (.then #(update-plays-checkpoint db-client play-id (inc play-page)))
        (.then #(query db-client (generate "plays"
                                           ["id" "game_id" "length" "players"]
                                           ["id"]
                                           plays)))
        (.then #(commit db-client))
        (.catch #(rollback db-client))
        (.finally #(release db-client))))))

(defn save-collection [database collection-maps]
  (query database
         (generate "player_collections"
                   ["username" "game_id" "last_updated" "own"]
                   ["username" "game_id"]
                   (->> collection-maps
                        (filter :own)
                        (map #(vector (:username %)
                                      (:id %)
                                      (.toISOString (js/Date.))
                                      (:own %)))))))

(defn insert-games [db-pool insertions new-checkpoint]
  (.then
   (client db-pool)
   (fn [db-client]
     (-> (begin db-client)
         (.then #(update-game-checkpoint db-client new-checkpoint))
         (.then #(js/Promise.all (map (partial query db-client) insertions)))
         (.then #(commit db-client))
         (.catch #(rollback db-client))
         (.finally #(release db-client))))))
