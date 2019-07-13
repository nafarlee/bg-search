const { Client } = require('pg');

const transpile = require('../transpile');

module.exports = async function search(req, res) {
  res.set('Cache-Control', `public, max-age=${60 * 60 * 24 * 7}`);
  const query = req.query.query || '';
  const order = req.query.order || 'bayes-rating';
  const direction = req.query.direction || 'desc';
  console.log({ query, order, direction });
  const sql = transpile(query);
  const client = new Client({
    user: 'postgres',
    database: 'postgres',
  });
  try {
    await client.connect();
    const { rows } = await client.query(sql);
    console.log(rows);
  } finally {
    client.end();
  }
};
