DROP TABLE IF EXISTS games CASCADE;
CREATE TABLE games (
  id INTEGER PRIMARY KEY,
  image TEXT,
  thumbnail TEXT,
  last_updated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  average_rating REAL,
  average_weight REAL,
  bayes_rating REAL,
  description TEXT,
  maximum_players SMALLINT CHECK (maximum_players > 0),
  maximum_playtime SMALLINT CHECK (maximum_playtime > 0),
  minimum_age SMALLINT CHECK (minimum_age > 0),
  minimum_players SMALLINT CHECK (minimum_players > 0),
  minimum_playtime SMALLINT CHECK (minimum_playtime > 0),
  primary_name TEXT,
  rating_deviation REAL,
  rating_votes INTEGER,
  weight_votes INTEGER,
  year SMALLINT
);


DROP TABLE IF EXISTS alternate_names CASCADE;
CREATE TABLE alternate_names (
  id INTEGER REFERENCES games ON DELETE CASCADE,
  alternate_name TEXT,
  PRIMARY KEY (id, alternate_name)
);

DROP TABLE IF EXISTS player_recommendations CASCADE;
CREATE TABLE player_recommendations (
  id INTEGER REFERENCES games ON DELETE CASCADE,
  players INT4RANGE,
  best INTEGER CHECK (best >= 0),
  recommended INTEGER CHECK (recommended >= 0),
  not_recommended INTEGER CHECK (not_recommended >= 0),
  PRIMARY KEY (id, players)
);


DROP TABLE IF EXISTS reimplementations CASCADE;
CREATE TABLE reimplementations (
  original INTEGER,
  reimplementation INTEGER,
  PRIMARY KEY (reimplementation, original)
);

DROP TABLE IF EXISTS collections CASCADE;
CREATE TABLE collections (
  item INTEGER,
  collection INTEGER,
  PRIMARY KEY (collection, item)
);

DROP TABLE IF EXISTS expansions CASCADE;
CREATE TABLE expansions (
  base INTEGER,
  expansion INTEGER,
  PRIMARY KEY (expansion, base)
);


DROP TABLE IF EXISTS publishers CASCADE;
CREATE TABLE publishers (
  id INTEGER PRIMARY KEY,
  publisher TEXT
);

DROP TABLE IF EXISTS games_publishers CASCADE;
CREATE TABLE games_publishers (
  game_id INTEGER REFERENCES games ON DELETE CASCADE,
  publisher_id INTEGER REFERENCES publishers ON DELETE RESTRICT,
  PRIMARY KEY (game_id, publisher_id)
);


DROP TABLE IF EXISTS mechanics CASCADE;
CREATE TABLE mechanics (
  id INTEGER PRIMARY KEY,
  mechanic TEXT
);

DROP TABLE IF EXISTS games_mechanics CASCADE;
CREATE TABLE games_mechanics (
  game_id INTEGER REFERENCES games ON DELETE CASCADE,
  mechanic_id INTEGER REFERENCES mechanics ON DELETE RESTRICT,
  PRIMARY KEY (game_id, mechanic_id)
);


DROP TABLE IF EXISTS families CASCADE;
CREATE TABLE families (
  id INTEGER PRIMARY KEY,
  family TEXT
);

DROP TABLE IF EXISTS games_families CASCADE;
CREATE TABLE games_families (
  game_id INTEGER REFERENCES games ON DELETE CASCADE,
  family_id INTEGER REFERENCES families ON DELETE RESTRICT,
  PRIMARY KEY (game_id, family_id)
);


DROP TABLE IF EXISTS artists CASCADE;
CREATE TABLE artists (
  id INTEGER PRIMARY KEY,
  artist TEXT
);

DROP TABLE IF EXISTS games_artists CASCADE;
CREATE TABLE games_artists (
  game_id INTEGER REFERENCES games ON DELETE CASCADE,
  artist_id INTEGER REFERENCES artists ON DELETE RESTRICT,
  PRIMARY KEY (game_id, artist_id)
);


DROP TABLE IF EXISTS categories CASCADE;
CREATE TABLE categories (
  id INTEGER PRIMARY KEY,
  category TEXT
);

DROP TABLE IF EXISTS games_categories CASCADE;
CREATE TABLE games_categories (
  game_id INTEGER REFERENCES games ON DELETE CASCADE,
  category_id INTEGER REFERENCES categories ON DELETE RESTRICT,
  PRIMARY KEY (game_id, category_id)
);


DROP TABLE IF EXISTS designers CASCADE;
CREATE TABLE designers (
  id INTEGER PRIMARY KEY,
  designer TEXT
);

DROP TABLE IF EXISTS games_designers CASCADE;
CREATE TABLE games_designers (
  game_id INTEGER REFERENCES games ON DELETE CASCADE,
  designer_id INTEGER REFERENCES designers ON DELETE RESTRICT,
  PRIMARY KEY (game_id, designer_id)
);
