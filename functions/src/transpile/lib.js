const FIELDS = [
  'id',
  'primary_name',
  'rating_votes',
  'average_rating',
  'bayes_rating',
  'rating_deviation',
  'average_weight',
  'weight_votes',
  'year',
  'minimum_age',
  'minimum_players',
  'maximum_players',
  'minimum_playtime',
  'maximum_playtime',
  'description',
];
const CONCATENATED_FIELDS = FIELDS.join(', ');

const simple = field => ({ value, negate = false }) => ({
  text: `SELECT ${CONCATENATED_FIELDS}
         FROM games
         WHERE ${field} ${negate ? '!' : ''}~~* {{}}`,
  values: [`%${value}%`],
});

const junction = ({
  table,
  field,
}) => ({ value, negate = false }) => ({
  text: `SELECT DISTINCT ${CONCATENATED_FIELDS.replace('id', 'a.id')}
         FROM games a, games_${table} ab, ${table} b
         WHERE a.id = ab.game_id
           AND ab.${field}_id = b.id
           AND ${field} ${negate ? '!' : ''}~~* {{}}`,
  values: [`%${value}%`],
});

const relational = field => ({ operator, value, negate = false }) => ({
  text: `SELECT ${CONCATENATED_FIELDS}
         FROM games
         WHERE ${negate ? 'NOT' : ''} ${field} ${operator} {{}} `,
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
  FIELDS,
  CONCATENATED_FIELDS,

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

  RECOMMENDED_PLAYERS: ({ operator, value, negate = false }) => ({
    text: `SELECT ${CONCATENATED_FIELDS.replace('id', 'a.id')}
           FROM games a, player_recommendations b
           WHERE a.id = b.id
             AND players && {{}}::int4range
             AND ${negate ? 'NOT' : ''} recommended > (best + not_recommended)`,
    values: [toRange(operator, value)],
  }),

  BEST_PLAYERS: ({ operator, value, negate = false }) => ({
    text: `SELECT ${CONCATENATED_FIELDS.replace('id', 'a.id')}
           FROM games a, player_recommendations b
           WHERE a.id = b.id
             AND players && {{}}::int4range
             AND ${negate ? 'NOT' : ''} best > (recommended + not_recommended)`,
    values: [toRange(operator, value)],
  }),

  EXPANSION: ({ negate = false }) => ({
    text: `SELECT ${CONCATENATED_FIELDS}
           FROM games
           LEFT JOIN expansions
             ON id = expansion
           WHERE base IS ${negate ? '' : 'NOT'} NULL`,
    values: null,
  }),

  COLLECTION: ({ negate = false }) => ({
    text: `SELECT ${CONCATENATED_FIELDS}
           FROM games
           LEFT JOIN collections
             ON id = collection
           WHERE item IS ${negate ? '' : 'NOT'} NULL`,
    values: null,
  }),

  REIMPLEMENTATION: ({ negate = false }) => ({
    text: `SELECT ${CONCATENATED_FIELDS}
           FROM games
           LEFT JOIN reimplementations
             ON id = reimplementation
           WHERE original IS ${negate ? '' : 'NOT'} NULL`,
    values: null,
  }),
};
