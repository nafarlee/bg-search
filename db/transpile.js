const FIELDS = 'primary_name, year';

const simple = field => (value, negate = false) => ({
  text: `SELECT ${FIELDS}
         FROM games
         WHERE ${field} ${negate ? '!' : ''}~~* $1`,
  values: [`%${value}%`],
});

module.exports.NAME = simple('primary_name');
module.exports.DESCRIPTION = simple('description');

module.exports.ARTIST = (value, negate = false) => ({
  text: `SELECT DISTINCT ${FIELDS}
         FROM games g, games_artists ga, artists a
         WHERE g.id = ga.game_id
           AND ga.artist_id = a.id
           AND artist ${negate ? '!' : ''}~~* $1`,
  values: [`%${value}%`],
});

module.exports.CATEGORY = (value, negate = false) => ({
  text: `SELECT DISTINCT ${FIELDS}
         FROM games g, games_categories gc, categories c
         WHERE g.id = gc.game_id
           AND gc.category_id = c.id
           AND category ${negate ? '!' : ''}~~* $1`,
  values: [`%${value}%`],
});
