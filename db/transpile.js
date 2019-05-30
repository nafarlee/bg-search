const FIELDS = 'primary_name, year';

const simple = field => (value, negate = false) => ({
  text: `SELECT ${FIELDS}
         FROM games
         WHERE ${field} ${negate ? '!' : ''}~~* $1`,
  values: [`%${value}%`],
});

const junction = ({
  table,
  field,
}) => (value, negate = false) => ({
  text: `SELECT DISTINCT ${FIELDS}
         FROM games a, games_${table} ab, ${table} b
         WHERE a.id = ab.game_id
           AND ab.${field}_id = b.id
           AND ${field} ${negate ? '!' : ''}~~* $1`,
  values: [`%${value}%`],
});

const relational = field => (operator, value, negate = false) => ({
  text: `SELECT ${FIELDS}
         FROM games
         WHERE ${negate ? 'NOT' : ''} ${field} ${operator} $1 `,
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

module.exports = {
  NAME: simple('primary_name'),
  DESCRIPTION: simple('description'),

  ARTIST: junction({ table: 'artists', field: 'artist' }),
  CATEGORY: junction({ table: 'categories', field: 'category' }),
  FAMILY: junction({ table: 'families', field: 'family' }),
  MECHANIC: junction({ table: 'mechanics', field: 'mechanic' }),
  PUBLISHER: junction({ table: 'publishers', field: 'publisher' }),
  DESIGNER: junction({ table: 'designers', field: 'designer' }),

  RATING_VOTES: relational('rating_votes'),
  AVERAGE_RATING: relational('average_rating'),
  GEEK_RATING: relational('bayes_rating'),
  RATING_DEVIATION: relational('rating_deviation'),
  AVERAGE_WEIGHT: relational('average_weight'),
  WEIGHT_VOTES: relational('weight_votes'),
  YEAR: relational('year'),
  AGE: relational('minimum_age'),
  MINIMUM_PLAYERS: relational('minimum_players'),
  MAXIMUM_PLAYERS: relational('maximum_players'),
  MINIMUM_PLAYTIME: relational('minimum_playtime'),
  MAXIMUM_PLAYTIME: relational('maximum_playtime'),

  RECOMMENDED_PLAYERS: (operator, value, negate = false) => ({
    text: `SELECT ${FIELDS}
           FROM games g, player_recommendations pr
           WHERE g.id = pr.id
             AND players && $1::int4range
             AND ${negate ? 'NOT' : ''} recommended > (best + not_recommended)`,
    values: [toRange(operator, value)],
  }),

  BEST_PLAYERS: (operator, value, negate = false) => ({
    text: `SELECT ${FIELDS}
           FROM games g, player_recommendations pr
           WHERE g.id = pr.id
             AND players && $1::int4range
             AND ${negate ? 'NOT' : ''} best > (recommended + not_recommended)`,
    values: [toRange(operator, value)],
  }),

  EXPANSION: (negate = false) => ({
    text: `SELECT ${FIELDS}
           FROM games
           LEFT JOIN expansions
             ON id = expansion
           WHERE base IS ${negate ? '' : 'NOT'} NULL`,
    values: null,
  }),
};
