const { Client } = require('pg');

const credentials = require('../../db-credentials');

module.exports = async function search(req, res) {
  res.set('Cache-Control', `public, max-age=${60 * 60 * 24 * 7}`);
  const { id } = req.params;
  const client = new Client(credentials);
  const sql = `SELECT
                 (SELECT JSON_OBJECT_AGG(players, medians)
                   FROM (SELECT players, PERCENTILE_CONT(0.5) WITHIN GROUP(ORDER BY length) AS medians
                          FROM plays
                          WHERE game_id = $1 AND players IS NOT NULL
                          GROUP BY players) AS sub) as median_playtimes_by_players,
                 (SELECT PERCENTILE_CONT(0.5) WITHIN GROUP(ORDER BY length)
                   FROM plays
                   WHERE game_id = $1) AS median_playtime,
                 (SELECT COUNT(*)
                   FROM plays
                   WHERE game_id = $1) AS recorded_play_count,
                 (SELECT ARRAY_AGG(mechanic)
                   FROM mechanics
                   INNER JOIN games_mechanics ON id = mechanic_id
                   WHERE game_id = $1) AS mechanics,
                 (SELECT ARRAY_AGG(category)
                   FROM categories
                   INNER JOIN games_categories ON id = category_id
                   WHERE game_id = $1) AS categories,
                 (SELECT ARRAY_AGG(family)
                   FROM families
                   INNER JOIN games_families ON id = family_id
                   WHERE game_id = $1) AS families,
                 (SELECT ARRAY_AGG(publisher)
                   FROM publishers
                   INNER JOIN games_publishers ON id = publisher_id
                   WHERE game_id = $1) AS publishers,
                 (SELECT ARRAY_AGG(artist)
                   FROM artists
                   INNER JOIN games_artists ON id = artist_id
                   WHERE game_id = $1) AS artists,
                 (SELECT ARRAY_AGG(designer)
                   FROM designers
                   INNER JOIN games_designers ON id = designer_id
                   WHERE game_id = $1) AS designers,
                 (SELECT ARRAY_AGG(alternate_name)
                   FROM alternate_names
                   WHERE id = $1) AS alternate_names,
                 (SELECT JSON_AGG(player_recommendations)
                   FROM player_recommendations
                   WHERE id = $1) AS player_recommendations,
                 games.id,
                 image,
                 average_rating,
                 average_weight,
                 bayes_rating,
                 description,
                 maximum_players,
                 maximum_playtime,
                 minimum_age,
                 minimum_players,
                 minimum_playtime,
                 primary_name,
                 rating_deviation,
                 rating_votes,
                 weight_votes,
                 year
               FROM games
               WHERE games.id = $1
               GROUP BY games.id`;
  try {
    await client.connect();
    const { rows: games } = await client.query(sql, [id]);
    if (games.length === 0) return res.send('No game with that ID!');
    const [game] = games;
    return res.render('games', { game });
  } finally {
    client.end();
  }
};
