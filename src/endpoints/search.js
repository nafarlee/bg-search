const { format } = require('url');

const { Client } = require('pg');

const transpile = require('../transpile');
const credentials = require('../../db-credentials');

module.exports = async function search(req, res) {
  res.set('Cache-Control', `public, max-age=${60 * 60 * 24 * 7}`);

  const query = req.query.query || '';
  const order = req.query.order || 'bayes_rating';
  const direction = req.query.direction || 'DESC';
  const offset = parseInt(req.query.offset, 10) || 0;

  console.log({
    offset,
    query,
    order,
    direction,
  });

  let sql;
  try {
    sql = transpile(query, order, direction, offset);
  } catch (err) {
    return res.status(400).send(err);
  }
  const client = new Client(credentials);
  try {
    await client.connect();
    const { rows: games } = await client.query(sql);
    const nextURL = format({
      protocol: req.protocol,
      host: req.get('host'),
      pathname: req.path,
      query: {
        ...req.query,
        offset: offset + games.length,
      },
    });
    return res.render('search', {
      games,
      nextURL,
      query,
      order,
      direction,
    });
  } finally {
    client.end();
  }
};
