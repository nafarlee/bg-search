const { description, id, name } = require('./lib');

function marshall(game) {
  const suggestedNumplayers = game
    .poll
    .find(x => x.$.name === 'suggested_numplayers');

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
    year: parseInt(game.yearpublished[0].$.value, 10),
    players: {
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
    },
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
