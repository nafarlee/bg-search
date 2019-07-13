const lib = require('./lib');
const language = require('../language');

function toSQL(predicates, intersect = true) {
  const joiningTerm = intersect ? 'INTERSECT' : 'UNION';
  return predicates.reduce((acc, cur) => {
    const isOR = cur.type === 'OR';
    const { text, values } = isOR
      ? toSQL(cur.terms, false)
      : lib[cur.tag](cur);

    let newText = acc.text.length === 0
      ? text
      : `${acc.text} ${joiningTerm} ${text}`;

    newText = isOR ? `(${newText})` : newText;

    return {
      text: newText,
      values: acc.values.concat(values || []),
    };
  }, { text: '', values: [] });
}

module.exports = function transpile(s, order, direction) {
  const predicates = language.tryParse(s);
  let { text, values } = toSQL(predicates);

  text = `SELECT primary_name, year
          FROM games
          INTERSECT
          ${text}
          ORDER BY ${order} ${direction}
          LIMIT 25`;

  const replacer = (x => () => `$${x++}`)(1);
  return {
    text: text.replace(/\{\{\}\}/g, replacer),
    values,
  };
};
