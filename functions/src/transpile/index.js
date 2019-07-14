const lib = require('./lib');
const language = require('../language');
const { FIELDS, CONCATENATED_FIELDS } = require('../transpile/lib');

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

module.exports = function transpile(s, order, direction, offset) {
  if (!FIELDS.includes(order)) throw new Error('SQL injection attempt!');
  if (direction !== 'ASC' && direction !== 'DESC') throw new Error('SQL injection attempt!');

  const predicates = language.tryParse(s);
  let { text, values } = toSQL(predicates);

  text = `SELECT ${CONCATENATED_FIELDS}
          FROM games
          INTERSECT
          ${text}
          ORDER BY ${order} ${direction}
          LIMIT 25 OFFSET {{}}`;

  values = [...values, offset];

  const replacer = (x => () => `$${x++}`)(1);
  return {
    text: text.replace(/\{\{\}\}/g, replacer),
    values,
  };
};
