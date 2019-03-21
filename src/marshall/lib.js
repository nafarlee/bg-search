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

module.exports = {
  description,
  id,
  name,
  year,
};
