export default {
  MEDIAN_PLAYTIME: ({ operator, value, negate = false }) => ({
    text: `SELECT game_id as id
           FROM play_medians
           WHERE players = 0
             AND ${negate ? 'NOT' : ''} median ${operator} {{}}`,
    values: [value],
  }),
};
