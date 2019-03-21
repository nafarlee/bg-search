const {
  age,
  description,
  id,
  name,
  players,
  playtime,
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
    playtime: playtime(game),
    age: age(game),
    ...links,
  };
}

module.exports = marshall;
