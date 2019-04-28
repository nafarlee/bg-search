DROP TABLE IF EXISTS games CASCADE;
CREATE TABLE games (
  id INTEGER PRIMARY KEY,
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
  reimplemented_by text[],
  weight_votes INTEGER,
  year SMALLINT
);

DROP TABLE IF EXISTS publishers CASCADE;
CREATE TABLE publishers (
  id INTEGER REFERENCES games ON DELETE CASCADE,
  publisher TEXT,
  PRIMARY KEY (id, publisher)
);

DROP TABLE IF EXISTS mechanics CASCADE;
CREATE TABLE mechanics (
  id INTEGER REFERENCES games ON DELETE CASCADE,
  mechanic TEXT,
  PRIMARY KEY (id, mechanic)
);

DROP TABLE IF EXISTS families CASCADE;
CREATE TABLE families (
  id INTEGER REFERENCES games ON DELETE CASCADE,
  family TEXT,
  PRIMARY KEY (id, family)
);

DROP TABLE IF EXISTS alternate_names CASCADE;
CREATE TABLE alternate_names (
  id INTEGER REFERENCES games ON DELETE CASCADE,
  alternate_name TEXT,
  PRIMARY KEY (id, alternate_name)
);

DROP TABLE IF EXISTS artists CASCADE;
CREATE TABLE artists (
  id INTEGER REFERENCES games ON DELETE CASCADE,
  artist TEXT,
  PRIMARY KEY (id, artist)
);

DROP TABLE IF EXISTS categories CASCADE;
CREATE TABLE categories (
  id INTEGER REFERENCES games ON DELETE CASCADE,
  category TEXT,
  PRIMARY KEY (id, category)
);

DROP TABLE IF EXISTS collections CASCADE;
CREATE TABLE collections (
  collection INTEGER REFERENCES games ON DELETE CASCADE,
  item INTEGER REFERENCES games ON DELETE CASCADE,
  PRIMARY KEY (collection, item)
);

DROP TABLE IF EXISTS designers CASCADE;
CREATE TABLE designers (
  id INTEGER REFERENCES games ON DELETE CASCADE,
  designer TEXT,
  PRIMARY KEY (id, designer)
);

DROP TABLE IF EXISTS expansions CASCADE;
CREATE TABLE expansions (
  expansion INTEGER REFERENCES games ON DELETE CASCADE,
  base INTEGER REFERENCES games ON DELETE CASCADE,
  PRIMARY KEY (expansion, base)
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
