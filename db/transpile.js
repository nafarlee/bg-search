const FIELDS = 'primary_name, year';

module.exports.name = (value, negate = false) => ({
  text: `SELECT ${FIELDS} FROM games WHERE primary_name ${negate ? '!' : ''}~~* $1`,
  values: [`%${value}%`],
});

module.exports.ARTIST = (value, negate = false) => ({
  text: `SELECT DISTINCT ${FIELDS}
         FROM games g, games_artists ga, artists a
         WHERE g.id = ga.game_id
           AND ga.artist_id = a.id
           AND artist ${negate ? '!' : ''}~~* $1`,
  values: [`%${value}%`],
});
