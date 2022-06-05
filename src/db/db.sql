CREATE TABLE IF NOT EXISTS globals (
  id INTEGER PRIMARY KEY DEFAULT 1,
  count INTEGER CHECK (count >= 1),
  play_id INTEGER CHECK (count >= 1),
  play_page INTEGER CHECK (count >= 1)
);
INSERT INTO globals
VALUES (1, 1, 1, 1);

CREATE TABLE IF NOT EXISTS games (
  id INTEGER PRIMARY KEY,
  language_dependence SMALLINT CHECK (language_dependence >= 0),
  image TEXT,
  thumbnail TEXT,
  last_updated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  average_rating REAL,
  average_weight REAL,
  bayes_rating REAL,
  steamdb_rating REAL GENERATED ALWAYS AS (average_rating - (average_rating - 5.0) * power(2, -log(rating_votes + 1))) STORED,
  description TEXT,
  maximum_players INTEGER CHECK (maximum_players >= 0),
  maximum_playtime INTEGER CHECK (maximum_playtime >= 0),
  minimum_age INTEGER CHECK (minimum_age >= 0),
  minimum_players INTEGER CHECK (minimum_players >= 0),
  minimum_playtime INTEGER CHECK (minimum_playtime >= 0),
  primary_name TEXT,
  rating_deviation REAL,
  rating_votes INTEGER,
  weight_votes INTEGER,
  year INTEGER
);

CREATE TABLE IF NOT EXISTS plays (
  id INTEGER PRIMARY KEY,
  game_id INTEGER REFERENCES games ON DELETE CASCADE,
  length INTEGER CHECK (length > 0) NOT NULL,
  players INTEGER CHECK (players > 0)
);


DROP MATERIALIZED VIEW play_medians;

CREATE MATERIALIZED VIEW play_medians AS
SELECT game_id,
       COUNT(id) AS count,
       0 AS players,
       PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY length) AS median
FROM plays
GROUP BY game_id
UNION
SELECT game_id,
       COUNT(id) AS count,
       players,
       PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY length) AS median
FROM plays
WHERE players IS NOT NULL
GROUP BY game_id, players;

CREATE UNIQUE INDEX play_medians_unique_index ON play_medians (game_id, players);

CREATE TABLE IF NOT EXISTS alternate_names (
  id INTEGER REFERENCES games ON DELETE CASCADE,
  alternate_name TEXT,
  PRIMARY KEY (id, alternate_name)
);

CREATE TABLE IF NOT EXISTS player_recommendations (
  id INTEGER REFERENCES games ON DELETE CASCADE,
  players INT4RANGE,
  best INTEGER CHECK (best >= 0),
  recommended INTEGER CHECK (recommended >= 0),
  not_recommended INTEGER CHECK (not_recommended >= 0),
  is_recommended BOOLEAN GENERATED ALWAYS AS (recommended >= best AND recommended >= not_recommended) STORED,
  is_best BOOLEAN GENERATED ALWAYS AS (best >= recommended AND best >= not_recommended) STORED,
  is_quorum BOOLEAN GENERATED ALWAYS AS ((best + recommended) >= (not_recommended * 13.0 / 7.0)) STORED,
  is_majority BOOLEAN GENERATED ALWAYS AS (best + recommended > not_recommended) STORED,
  PRIMARY KEY (id, players)
);
CREATE INDEX IF NOT EXISTS player_recommendations_is_quorum_index on player_recommendations (is_quorum);
CREATE INDEX IF NOT EXISTS player_recommendations_players_index ON player_recommendations USING gist (players);


CREATE TABLE IF NOT EXISTS reimplementations (
  original INTEGER,
  reimplementation INTEGER,
  PRIMARY KEY (reimplementation, original)
);

CREATE TABLE IF NOT EXISTS collections (
  item INTEGER,
  collection INTEGER,
  PRIMARY KEY (collection, item)
);

CREATE TABLE IF NOT EXISTS expansions (
  base INTEGER,
  expansion INTEGER,
  PRIMARY KEY (expansion, base)
);


CREATE TABLE IF NOT EXISTS publishers (
  id INTEGER PRIMARY KEY,
  publisher TEXT
);

CREATE TABLE IF NOT EXISTS games_publishers (
  game_id INTEGER REFERENCES games ON DELETE CASCADE,
  publisher_id INTEGER REFERENCES publishers ON DELETE RESTRICT,
  PRIMARY KEY (game_id, publisher_id)
);


CREATE TABLE IF NOT EXISTS mechanics (
  id INTEGER PRIMARY KEY,
  mechanic TEXT
);

CREATE TABLE IF NOT EXISTS games_mechanics (
  game_id INTEGER REFERENCES games ON DELETE CASCADE,
  mechanic_id INTEGER REFERENCES mechanics ON DELETE RESTRICT,
  PRIMARY KEY (game_id, mechanic_id)
);


CREATE TABLE IF NOT EXISTS families (
  id INTEGER PRIMARY KEY,
  family TEXT
);

CREATE TABLE IF NOT EXISTS games_families (
  game_id INTEGER REFERENCES games ON DELETE CASCADE,
  family_id INTEGER REFERENCES families ON DELETE RESTRICT,
  PRIMARY KEY (game_id, family_id)
);


CREATE TABLE IF NOT EXISTS artists (
  id INTEGER PRIMARY KEY,
  artist TEXT
);

CREATE TABLE IF NOT EXISTS games_artists (
  game_id INTEGER REFERENCES games ON DELETE CASCADE,
  artist_id INTEGER REFERENCES artists ON DELETE RESTRICT,
  PRIMARY KEY (game_id, artist_id)
);


CREATE TABLE IF NOT EXISTS categories (
  id INTEGER PRIMARY KEY,
  category TEXT
);

CREATE TABLE IF NOT EXISTS games_categories (
  game_id INTEGER REFERENCES games ON DELETE CASCADE,
  category_id INTEGER REFERENCES categories ON DELETE RESTRICT,
  PRIMARY KEY (game_id, category_id)
);


CREATE TABLE IF NOT EXISTS designers (
  id INTEGER PRIMARY KEY,
  designer TEXT
);

CREATE TABLE IF NOT EXISTS games_designers (
  game_id INTEGER REFERENCES games ON DELETE CASCADE,
  designer_id INTEGER REFERENCES designers ON DELETE RESTRICT,
  PRIMARY KEY (game_id, designer_id)
);

CREATE TABLE IF NOT EXISTS player_collections (
  username TEXT,
  game_id INTEGER REFERENCES games ON DELETE CASCADE,
  last_updated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  own BOOLEAN,
  PRIMARY KEY (username, game_id)
);


CREATE INDEX IF NOT EXISTS games_average_rating_index ON games (average_rating);
CREATE INDEX IF NOT EXISTS games_rating_votes_index ON games (rating_votes);
CREATE INDEX IF NOT EXISTS games_steamdb_rating_index ON games (steamdb_rating);
