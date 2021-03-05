(ns sql)

(def ^:private get-game-sql
  "SELECT
    (SELECT JSON_OBJECT_AGG(players, medians)
      FROM (SELECT players, PERCENTILE_CONT(0.5) WITHIN GROUP(ORDER BY length) AS medians
            FROM plays
            WHERE game_id = $1 AND players IS NOT NULL
            GROUP BY players) AS sub) as median_playtimes_by_players,
    (SELECT PERCENTILE_CONT(0.5) WITHIN GROUP(ORDER BY length)
      FROM plays
      WHERE game_id = $1) AS median_playtime,
    (SELECT COUNT(*)
      FROM plays
      WHERE game_id = $1) AS recorded_play_count,
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
    image,
    average_rating,
    average_weight,
    bayes_rating,
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
      (.then #(-> % .-rows first .-id some?))))

(defn game? [database id]
  (-> database
      (.query "SELECT id FROM games WHERE id = $1 LIMIT 1" #js[id])
      (.then #(-> % .-rows first .-id some?))))

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
                   ((juxt #(.-play_id %) #(.-play_page %))))))))

(defn mobius-games [database]
  (.query database "UPDATE globals SET count = $1 WHERE id = $2" #js[1 1]))

(defn begin [database]
  (.query database "BEGIN"))

(defn commit [database]
  (.query database "COMMIT"))

(defn rollback [database]
  (.query database "ROLLBACK"))

(defn update-game-checkpoint [database checkpoint]
  (.query database "UPDATE globals SET count = $1 WHERE id = $2" #js[checkpoint 1]))
