const simple = (field) => ({ value, negate = false }) => ({
  text: `SELECT id
         FROM games
         WHERE ${field} ${negate ? '!' : ''}~~* {{}}`,
  values: [`%${value}%`],
});

const junction = ({
  table,
  field,
}) => ({ value, negate = false }) => ({
  text: `SELECT a.id
         FROM games a, games_${table} ab, ${table} b
         WHERE a.id = ab.game_id
           AND ab.${field}_id = b.id
         GROUP BY a.id
         HAVING BOOL_OR(${field} ~~* {{}}) != ${negate}`,
  values: [`%${value}%`],
});

const relational = (field) => ({ operator, value, negate = false }) => ({
  text: `SELECT id
         FROM games
         WHERE ${negate ? 'NOT' : ''} ${field} ${operator} {{}}`,
  values: [value],
});

function toRange(operator, value) {
  switch (operator) {
    case '>': return `(${value},)`;
    case '<': return `(,${value})`;
    case '<=': return `(,${value}]`;
    case '>=': return `[${value},)`;
    case '=': return `[${value},${value}]`;
    default: throw new Error('Invalid operator');
  }
}

export default {
  __simple: simple,
  __junction: junction,
  __relational: relational,

  RECOMMENDED_PLAYERS: ({ operator, value, negate = false }) => ({
    text: `SELECT a.id
           FROM games a, player_recommendations b
           WHERE a.id = b.id
             AND players && {{}}::int4range
             AND ${negate ? 'NOT' : ''} recommended > (best + not_recommended)`,
    values: [toRange(operator, value)],
  }),

  BEST_PLAYERS: ({ operator, value, negate = false }) => ({
    text: `SELECT a.id
           FROM games a, player_recommendations b
           WHERE a.id = b.id
             AND players && {{}}::int4range
             AND ${negate ? 'NOT' : ''} best > (recommended + not_recommended)`,
    values: [toRange(operator, value)],
  }),

  QUORUM_PLAYERS: ({ operator, value, negate = false }) => ({
    text: `SELECT a.id
           FROM games a, player_recommendations b
           WHERE a.id = b.id
             AND players && {{}}::int4range
             AND ${negate ? 'NOT' : ''} (best + recommended) >= (not_recommended / 3 * 7)`,
    values: [toRange(operator, value)],
  }),

  MEDIAN_PLAYTIME: ({ operator, value, negate = false }) => ({
    text: `SELECT game_id as id
           FROM play_medians
           WHERE players = 0
             AND ${negate ? 'NOT' : ''} median ${operator} {{}}`,
    values: [value],
  }),

  EXPANSION: ({ negate = false }) => ({
    text: `SELECT id
           FROM games
           LEFT JOIN expansions
             ON id = expansion
           WHERE base IS ${negate ? '' : 'NOT'} NULL`,
    values: null,
  }),

  COLLECTION: ({ negate = false }) => ({
    text: `SELECT id
           FROM games
           LEFT JOIN collections
             ON id = collection
           WHERE item IS ${negate ? '' : 'NOT'} NULL`,
    values: null,
  }),

  REIMPLEMENTATION: ({ negate = false }) => ({
    text: `SELECT id
           FROM games
           LEFT JOIN reimplementations
             ON id = reimplementation
           WHERE original IS ${negate ? '' : 'NOT'} NULL`,
    values: null,
  }),
};
