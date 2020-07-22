const { Client } = require('pg');

const credentials = require('../../db-credentials');

module.exports = async function search(req, res) {
  res.set('Cache-Control', `public, max-age=${60 * 60 * 24 * 7}`);
  const { id } = req.params;
  const client = new Client(credentials);
  const sql = `SELECT
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
