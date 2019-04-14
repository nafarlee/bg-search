CREATE TABLE games (
  id INTEGER PRIMARY KEY,
  last_updated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  alternate_names TEXT[],
  artists TEXT[],
  average_rating REAL,
  average_weight REAL,
  bayes_rating REAL,
  categories TEXT[],
  contained_in TEXT[],
  description TEXT,
  designers TEXT[],
  expanded_by TEXT[],
  families TEXT[],
  maximum_players SMALLINT CHECK (max_players > 0),
  maximum_playtime SMALLINT CHECK (maximum_playtime > 0),
  mechanics TEXT[],
  minimum_age SMALLINT CHECK (minimum_age > 0),
  minimum_players SMALLINT CHECK (minimum_players > 0),
  minimum_playtime SMALLINT CHECK (minimum_playtime > 0),
  primary_name TEXT,
  publishers TEXT[],
  rating_deviation REAL,
  rating_votes INTEGER,
  reimplemented_by text[],
  weight_votes INTEGER,
  year SMALLINT
);

CREATE TABLE player_recommendations (
  id INTEGER REFERENCES games ON DELETE CASCADE,
  players INT4RANGE,
  best INTEGER CHECK (best >= 0),
  recommended INTEGER CHECK (recommended >= 0),
  not_recommended INTEGER CHECK (not_recommended >= 0),
  PRIMARY KEY (id, players)
);
