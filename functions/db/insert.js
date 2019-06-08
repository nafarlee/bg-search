#!/usr/bin/env node
const _ = require('lodash');

function toSQL(table, columns, chunks) {
  const values = _.flatten(chunks);
  const positions = _(values)
    .map((_v, i) => `$${i + 1}`)
    .chunk(columns.length)
    .map(chunk => `(${chunk.join(', ')})`)
    .join(', ');

  return [
    `INSERT INTO ${table} (${columns.join(', ')})
     VALUES ${positions};
    `,
    values,
  ];
}

const kvInsert = (table, columns, prop = table) => (game) => {
  if (_.isEmpty(game[prop])) return null;

  return toSQL(
    table,
    columns,
    game[prop].map(p => [p.id, p.value]),
  );
};

const junctionInsert = (table, columns, prop = table) => (game) => {
  if (_.isEmpty(game[prop])) return null;

  return toSQL(
    table,
    columns,
    game[prop].map(({ id }) => [game.id, id]),
  );
};

const soloJunctionInsert = (table, columns, props) => (game) => {
  let leftToRight = game[props[0]];
  let rightToLeft = game[props[1]];
  if (_.every([leftToRight, rightToLeft], _.isEmpty)) {
    return null;
  }

  leftToRight = game[props[0]] || [];
  rightToLeft = game[props[1]] || [];

  return toSQL(
    table,
    columns,
    _.concat(
      rightToLeft.map(({ id }) => [id, game.id]),
      leftToRight.map(({ id }) => [game.id, id]),
    ),
  );
};

const tables = {
  games(game) {
    const fields = {
      id: game.id,
      image: game.image,
      thumbnail: game.thumbnail,
      average_rating: game['average-rating'],
      average_weight: game['average-weight'],
      bayes_rating: game['bayes-rating'],
      description: game.description,
      maximum_players: game['maximum-players'],
      maximum_playtime: game['maximum-playtime'],
      minimum_age: game['minimum-age'],
      minimum_players: game['minimum-players'],
      minimum_playtime: game['minimum-playtime'],
      primary_name: game['primary-name'],
      rating_deviation: game['rating-deviation'],
      rating_votes: game['rating-votes'],
      weight_votes: game['weight-votes'],
      year: game.year,
    };

    const columns = _.keys(fields);
    const values = _.map(columns, c => fields[c]);

    return toSQL('games', columns, [values]);
  },

  alternate_names({ id, 'alternate-names': alternateNames }) {
    if (_.isEmpty(alternateNames)) return null;

    return toSQL(
      'alternate_names',
      ['id', 'alternate_name'],
      alternateNames.map(n => [id, n]),
    );
  },

  reimplementations: soloJunctionInsert(
    'reimplementations',
    ['original', 'reimplementation'],
    ['reimplemented-by', 'reimplements'],
  ),

  collections: soloJunctionInsert(
    'collections',
    ['item', 'collection'],
    ['contained-in', 'contains'],
  ),

  expansions: soloJunctionInsert(
    'expansions',
    ['base', 'expansion'],
    ['expanded-by', 'expands'],
  ),

  publishers: kvInsert('publishers', ['id', 'publisher']),
  games_publishers: junctionInsert(
    'games_publishers',
    ['game_id', 'publisher_id'],
    'publishers',
  ),

  mechanics: kvInsert('mechanics', ['id', 'mechanic']),
  games_mechanics: junctionInsert(
    'games_mechanics',
    ['game_id', 'mechanic_id'],
    'mechanics',
  ),

  families: kvInsert('families', ['id', 'family']),
  games_families: junctionInsert(
    'games_families',
    ['game_id', 'family_id'],
    'families',
  ),

  artists: kvInsert('artists', ['id', 'artist']),
  games_artists: junctionInsert(
    'games_artists',
    ['game_id', 'artist_id'],
    'artists',
  ),

  categories: kvInsert('categories', ['id', 'category']),
  games_categories: junctionInsert(
    'games_categories',
    ['game_id', 'category_id'],
    'categories',
  ),

  designers: kvInsert('designers', ['id', 'designer']),
  games_designers: junctionInsert(
    'games_designers',
    ['game_id', 'designer_id'],
    'designers',
  ),
};

function insert(game) {
  return _(tables)
    .map(f => f(game))
    .compact()
    .value();
}

module.exports = insert;
