#!/usr/bin/env node
const _ = require('lodash');

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

  alternate_names(game) {
    const values = _(game['alternate-names'])
      .map(n => [game.id, n])
      .flatten()
      .value();

    const midText = _(values)
      .map((_v, i) => `$${i + 1}`)
      .chunk(2)
      .map(pair => `(${pair.join(', ')})`)
      .join(', ');

    return [
      `INSERT INTO alternate_names id, alternate_name
       VALUES ${midText};`,
      values,
    ];
  },
};

function insert(game) {
  return tables.games(game);
}

module.exports = insert;
