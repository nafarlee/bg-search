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
    'primary-name': primaries[0].$.value,
    'alternate-names': alternates.map(x => x.$.value),
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

  const result = {
    'minimum-players': parseInt(game.minplayers[0].$.value, 10),
    'maximum-players': parseInt(game.maxplayers[0].$.value, 10),
  };

  if (suggestedNumplayers.$.totalvotes !== '0') {
    result['community-recommended-players'] = {
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
    };
  }

  return result;
}

function playtime(game) {
  return {
    minimum: parseInt(game.minplaytime[0].$.value, 10),
    maximum: parseInt(game.maxplaytime[0].$.value, 10),
  };
}

function age(game) {
  return {
    minimum: parseInt(game.minage[0].$.value, 10),
  };
}

function links(game) {
  const sections = {
    boardgamecategory(record) {
      return ['categories', record.$.value];
    },
    boardgamemechanic(record) {
      return ['mechanics', record.$.value];
    },
    boardgamefamily(record) {
      return ['families', record.$.value];
    },
    boardgameexpansion(record) {
      const category = record.$.inbound ? 'expands' : 'expanded-by';
      return [category, parseInt(record.$.id, 10)];
    },
    boardgamecompilation(record) {
      const category = record.$.inbound ? 'contains' : 'contained-in';
      return [category, parseInt(record.$.id, 10)];
    },
    boardgameimplementation(record) {
      const category = record.$.inbound ? 'reimplements' : 'reimplimented-by';
      return [category, parseInt(record.$.id, 10)];
    },
    boardgamedesigner(record) {
      return ['designers', record.$.value];
    },
    boardgameartist(record) {
      return ['artists', record.$.value];
    },
    boardgamepublisher(record) {
      return ['publishers', record.$.value];
    },
    boardgameintegration(record) {
      return ['integrates-with', record.$.value];
    },
  };

  return game
    .link
    .reduce((accum, current) => {
      const [group, item] = sections[current.$.type](current);
      accum[group] = accum[group] || [];
      accum[group].push(item);
      return accum;
    }, {});
}

function ratings(game) {
  const getValue = property => game.statistics[0].ratings[0][property][0].$.value;
  return {
    count: getValue('usersrated'),
    average: getValue('average'),
    bayes: getValue('bayesaverage'),
    deviation: getValue('stddev'),
  };
}

function weight(game) {
  return {
    count: game.statistics[0].ratings[0].numweights[0].$.value,
    average: game.statistics[0].ratings[0].averageweight[0].$.value,
  };
}

module.exports = {
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
};
