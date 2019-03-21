const {
  description,
  id,
  name,
  players,
  year,
} = require('./lib');

function marshall(game) {
  const sections = {
    boardgamecategory: 'categories',
    boardgamemechanic: 'mechanics',
    boardgamefamily: 'families',
    boardgameexpansion: 'expansions',
    boardgamecompilation: 'compilations',
    boardgameimplementation: 'implementations',
    boardgamedesigner: 'designers',
    boardgameartist: 'artists',
    boardgamepublisher: 'publishers',
  };

  const links = game.link.reduce((accum, current) => {
    const section = sections[current.$.type];
    accum[section] = accum[section] || [];
    accum[section].push(parseInt(current.$.id, 10));
    return accum;
  }, {});

  return {
    id: id(game),
    name: name(game),
    description: description(game),
    year: year(game),
    players: players(game),
    playtime: {
      minimum: parseInt(game.minplaytime[0].$.value, 10),
      maximum: parseInt(game.maxplaytime[0].$.value, 10),
    },
    age: {
      minimum: parseInt(game.minage[0].$.value, 10),
    },
    ...links,
  };
}

module.exports = marshall;
