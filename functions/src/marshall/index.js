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
    'api-version': 3,
    id: id(game),
    image: game.image[0],
    thumbnail: game.thumbnail[0],
    ...name(game),
    description: description(game),
    year: year(game),
    ...players(game),
    ...playtime(game),
    ...age(game),
    ...ratings(game),
    ...weight(game),
    ...links(game),
    'last-updated': (new Date()).toString(),
  };
}

module.exports = marshall;
