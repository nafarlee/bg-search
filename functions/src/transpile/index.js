const transpile = require('./lib');
const language = require('../language');

function query(s) {
  return language
    .tryParse(s)
    .reduce((acc, cur) => {
      if (cur.type === 'OR') return acc;
      const { text, values } = transpile[cur.tag](cur);
      return {
        text: (acc.text.length === 0) ? text : `${acc.text} INTERSECT ${text}`,
        values: acc.values.concat(values || []),
      };
    }, { text: '', values: [] });
}

module.exports = query;
