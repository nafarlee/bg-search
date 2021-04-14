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
             AND ${negate ? 'NOT' : ''} (best + recommended) >= (not_recommended / 3.0 * 7.0)`,
    values: [toRange(operator, value)],
  }),

  MEDIAN_PLAYTIME: ({ operator, value, negate = false }) => ({
    text: `SELECT game_id as id
           FROM play_medians
           WHERE players = 0
             AND ${negate ? 'NOT' : ''} median ${operator} {{}}`,
    values: [value],
  }),
};
