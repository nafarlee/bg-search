const { Client } = require('pg');

const credentials = require('../../db-credentials');

module.exports = async function search(req, res) {
  res.set('Cache-Control', `public, max-age=${60 * 60 * 24 * 7}`);
  const { id } = req.params;
  const client = new Client(credentials);
  const sql = `SELECT primary_name
               FROM games
               WHERE id = $1`;
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
