const { Client } = require('pg');

const credentials = require('../../db-credentials');

module.exports = async function search(req, res) {
  res.set('Cache-Control', `public, max-age=${60 * 60 * 24 * 7}`);
  const { id } = req.params;
  const client = new Client(credentials);
  const sql = `SELECT
                 ARRAY_AGG(mechanic) AS mechanics,
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
               INNER JOIN games_mechanics ON games.id = games_mechanics.game_id
               INNER JOIN mechanics ON mechanic_id = mechanics.id
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
