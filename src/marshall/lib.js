const { partition } = require('lodash');

function id(game) {
  return parseInt(game.$.id, 10);
}

function name(game) {
  const [
    primaries,
    alternates,
  ] = partition(game.name, record => record.$.type === 'primary');

  return {
    primary: primaries[0].$.value,
    alternates: alternates.map(x => x.$.value),
  };
}

function description(game) {
  return game.description[0];
}

function year(game) {
  return parseInt(game.yearpublished[0].$.value, 10);
}

function players(game) {
  const suggestedNumplayers = game
    .poll
    .find(x => x.$.name === 'suggested_numplayers');

  return {
    minimum: parseInt(game.minplayers[0].$.value, 10),
    maximum: parseInt(game.maxplayers[0].$.value, 10),
    community: {
      votes: parseInt(suggestedNumplayers.$.totalvotes, 10),
      counts: suggestedNumplayers.results.reduce((obj, current) => {
        const results = {};
        current.result.forEach((x) => {
          results[x.$.value.toLowerCase().replace(/ /g, '-')] = parseInt(x.$.numvotes, 10);
        });
        const count = current.$.numplayers;
        obj[count] = results;
        return obj;
      }, {}),
    },
  };
}

function playtime(game) {
  return {
    minimum: parseInt(game.minplaytime[0].$.value, 10),
    maximum: parseInt(game.maxplaytime[0].$.value, 10),
  };
}

module.exports = {
  description,
  id,
  name,
  players,
  playtime,
  year,
};
