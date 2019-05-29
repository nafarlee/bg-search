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
};
