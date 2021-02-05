import _ from 'lodash';

export function id(game) {
  return parseInt(game.$.id, 10);
}

export function name(game) {
  const [
    primaries,
    alternates,
  ] = _.partition(game.name, (record) => record.$.type === 'primary');

  return {
    'primary-name': primaries[0].$.value,
    'alternate-names': alternates.map((x) => x.$.value),
  };
}

export function description(game) {
  return game.description[0];
}

export function year(game) {
  return parseInt(game.yearpublished[0].$.value, 10) || 0;
}

export function players(game) {
  const suggestedNumplayers = game
    .poll
    .find((x) => x.$.name === 'suggested_numplayers');

  const result = {
    'minimum-players': parseInt(game.minplayers[0].$.value, 10) || 0,
    'maximum-players': parseInt(game.maxplayers[0].$.value, 10) || 0,
  };

  if (suggestedNumplayers.$.totalvotes !== '0') {
    result['community-recommended-players'] = {
      votes: parseInt(suggestedNumplayers.$.totalvotes, 10) || 0,
      counts: suggestedNumplayers.results.reduce((obj, current) => {
        if (!current.result) return obj;

        const results = {};
        current.result.forEach((x) => {
          results[x.$.value.toLowerCase().replace(/ /g, '-')] = parseInt(x.$.numvotes, 10) || 0;
        });
        const count = current.$.numplayers;
        obj[count] = results;
        return obj;
      }, {}),
    };
  }

  return result;
}

export function playtime(game) {
  return {
    'minimum-playtime': parseInt(game.minplaytime[0].$.value, 10) || 0,
    'maximum-playtime': parseInt(game.maxplaytime[0].$.value, 10) || 0,
  };
}

export function age(game) {
  return {
    'minimum-age': parseInt(game.minage[0].$.value, 10) || 0,
  };
}

export function links({ link = [] }) {
  const sections = {
    boardgamecategory(record) {
      return ['categories', {
        id: parseInt(record.$.id, 10),
        value: record.$.value,
      }];
    },
    boardgamemechanic(record) {
      return ['mechanics', {
        id: parseInt(record.$.id, 10),
        value: record.$.value,
      }];
    },
    boardgamefamily(record) {
      return ['families', {
        id: parseInt(record.$.id, 10),
        value: record.$.value,
      }];
    },
    boardgameexpansion(record) {
      const category = record.$.inbound ? 'expands' : 'expanded-by';
      return [category, {
        id: parseInt(record.$.id, 10),
        value: record.$.value,
      }];
    },
    boardgamecompilation(record) {
      const category = record.$.inbound ? 'contains' : 'contained-in';
      return [category, {
        id: parseInt(record.$.id, 10),
        value: record.$.value,
      }];
    },
    boardgameimplementation(record) {
      const category = record.$.inbound ? 'reimplements' : 'reimplemented-by';
      return [category, {
        id: parseInt(record.$.id, 10),
        value: record.$.value,
      }];
    },
    boardgamedesigner(record) {
      return ['designers', {
        id: parseInt(record.$.id, 10),
        value: record.$.value,
      }];
    },
    boardgameartist(record) {
      return ['artists', {
        id: parseInt(record.$.id, 10),
        value: record.$.value,
      }];
    },
    boardgamepublisher(record) {
      return ['publishers', {
        id: parseInt(record.$.id, 10),
        value: record.$.value,
      }];
    },
    boardgameintegration(record) {
      return ['integrates-with', {
        id: parseInt(record.$.id, 10),
        value: record.$.value,
      }];
    },
  };

  return link
    .reduce((accum, current) => {
      const [group, item] = sections[current.$.type](current);
      accum[group] = accum[group] || [];
      accum[group].push(item);
      return accum;
    }, {});
}

export function ratings(game) {
  const getValue = (property) => game.statistics[0].ratings[0][property][0].$.value;
  return {
    'rating-votes': parseInt(getValue('usersrated'), 10) || 0,
    'average-rating': parseFloat(getValue('average')) || 0,
    'bayes-rating': parseFloat(getValue('bayesaverage')) || 0,
    'rating-deviation': parseFloat(getValue('stddev')) || 0,
  };
}

export function weight(game) {
  return {
    'weight-votes': parseInt(game.statistics[0].ratings[0].numweights[0].$.value, 10) || 0,
    'average-weight': parseFloat(game.statistics[0].ratings[0].averageweight[0].$.value) || 0,
  };
}
