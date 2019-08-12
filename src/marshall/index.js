const { get } = require('lodash');

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
    image: get(game, ['image', 0], null),
    thumbnail: get(game, ['thumbnail', 0], null),
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
