import language from '../language/index';
import * as lib from './lib';

function toSQL(predicates, intersect = true) {
  const joiningTerm = intersect ? 'INTERSECT ALL' : 'UNION ALL';
  return predicates.reduce((acc, cur) => {
    const isOR = cur.type === 'OR';
    const result = isOR
      ? toSQL(cur.terms, false)
      : lib[cur.tag](cur);
    let { text } = result;

    text = isOR ? `(${text})` : text;

    text = acc.text.length === 0
      ? text
      : `${acc.text} ${joiningTerm} ${text}`;

    return {
      text,
      values: acc.values.concat(result.values || []),
    };
  }, { text: '', values: [] });
}

export default function transpile(s, order, direction, offset) {
  if (!lib.FIELDS.includes(order)) throw new Error('SQL injection attempt!');
  if (direction !== 'ASC' && direction !== 'DESC') throw new Error('SQL injection attempt!');

  const predicates = language.tryParse(s);
  let { text, values } = toSQL(predicates);

  const from = (text.length === 0)
    ? 'games'
    : `(${text}) AS GameSubquery NATURAL INNER JOIN games`;

  text = `SELECT DISTINCT ${lib.CONCATENATED_FIELDS}
          FROM ${from}
          ORDER BY ${order} ${direction}
          LIMIT 25 OFFSET {{}}`;

  values = [...values, offset];

  const replacer = ((x) => () => `$${x++}`)(1);
  return {
    text: text.replace(/\{\{\}\}/g, replacer),
    values,
  };
}
