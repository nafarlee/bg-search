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

    const keys = Object.keys(fields);
    const positions = keys.map((_k, i) => `$${i + 1}`).join(', ');

    return [
      `INSERT INTO games (${keys.join(', ')})
       VALUES ( ${positions} );`,
      keys.map(k => fields[k]),
    ];
  },

  alternate_names({ id, 'alternate-names': alternateNames }) {
    if (_.isEmpty(alternateNames)) return null;

    return toSQL(
      'alternate_names',
      ['id', 'alternate_name'],
      alternateNames.map(n => [id, n]),
    );
  },

  reimplementations(game) {
    if (_.isEmpty(game.reimplements) && _.isEmpty(game['reimplemented-by'])) {
      return null;
    }

    const reimplements = _(game.reimplements)
      .map(({ id }) => [id, game.id])
      .flatten()
      .value();

    const reimplementedBy = _(game['reimplemented-by'])
      .map(({ id }) => [game.id, id])
      .flatten()
      .value();

    const values = _.concat(reimplements, reimplementedBy);

    const midText = _(values)
      .map((_v, i) => `$${i + 1}`)
      .chunk(2)
      .map(pair => `(${pair.join(', ')})`)
      .join(', ');

    return [
      `INSERT INTO reimplementations (original, reimplementation)
       VALUES ${midText};`,
      values,
    ];
  },
};

function insert(game) {
  return _(tables)
    .map(f => f(game))
    .compact()
    .value();
}

module.exports = insert;
