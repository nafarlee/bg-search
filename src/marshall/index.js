const {
  age,
  description,
  id,
  links,
  name,
  players,
  playtime,
  ratings,
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
    ...links(game),
  };
}

module.exports = marshall;
