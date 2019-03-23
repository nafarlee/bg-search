const {
  age,
  description,
  id,
  links,
  name,
  players,
  playtime,
  ratings,
  weight,
  year,
} = require('./lib');

function marshall(game) {
  return {
    id: id(game),
    name: name(game),
    description: description(game),
    year: year(game),
    players: players(game),
    playtime: playtime(game),
    age: age(game),
    ratings: ratings(game),
    weight: weight(game),
    ...links(game),
    'last-updated': (new Date()).toString(),
  };
}

module.exports = marshall;
