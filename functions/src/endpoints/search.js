const { Client } = require('pg');

const transpile = require('../transpile');
const views = require('../views');

module.exports = async function search(req, res) {
  res.set('Cache-Control', `public, max-age=${60 * 60 * 24 * 7}`);
  const query = req.query.query || '';
  const order = req.query.order || 'bayes_rating';
  const direction = req.query.direction || 'DESC';
  const offset = req.query.offset || 0;
  console.log({ query, order, direction });
  const sql = transpile(query, order, direction, offset);
  const client = new Client({
    user: 'postgres',
    database: 'postgres',
  });
  try {
    await client.connect();
    const { rows } = await client.query(sql);
    res
      .status(200)
      .send(views.search({ req, fnName: 'search', games: rows }));
  } finally {
    client.end();
  }
};
