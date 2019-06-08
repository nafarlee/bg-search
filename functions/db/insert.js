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

  reimplementations({
    reimplements = [],
    id: gameID,
    'reimplemented-by': reimplementedBy = [],
  }) {
    if (_.isEmpty(reimplements) && _.isEmpty(reimplementedBy)) {
      return null;
    }

    return toSQL(
      'reimplementations',
      ['original', 'reimplementation'],
      _.concat(
        reimplements.map(({ id }) => [id, gameID]),
        reimplementedBy.map(({ id }) => [gameID, id]),
      ),
    );
  },

  collections({ contains = [], id: gameID, 'contained-in': containedIn = [] }) {
    if (_.isEmpty(contains) && _.isEmpty(containedIn)) return null;

    return toSQL(
      'collections',
      ['item', 'collection'],
      _.concat(
        contains.map(({ id }) => [id, gameID]),
        containedIn.map(({ id }) => [gameID, id]),
      ),
    );
  },

  expansions({ expands = [], id: gameID, 'expanded-by': expandedBy = [] }) {
    if (_.isEmpty(expands) && _.isEmpty(expandedBy)) return null;

    return toSQL(
      'expansions',
      ['base', 'expansion'],
      _.concat(
        expands.map(({ id }) => [id, gameID]),
        expandedBy.map(({ id }) => [gameID, id]),
      ),
    );
  },

  publishers({ publishers }) {
    if (_.isEmpty(publishers)) return null;

    return toSQL(
      'publishers',
      ['id', 'publisher'],
      publishers.map(p => [p.id, p.value]),
    );
  },

  games_publishers({ publishers, id }) {
    if (_.isEmpty(publishers)) return null;

    return toSQL(
      'games_publishers',
      ['game_id', 'publisher_id'],
      publishers.map(({ value }) => [id, value]),
    );
  },
};

function insert(game) {
  return _(tables)
    .map(f => f(game))
    .compact()
    .value();
}

module.exports = insert;
