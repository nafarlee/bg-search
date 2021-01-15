const { format } = require('url');

const { Client } = require('pg');

const transpile = require('../transpile');
const credentials = require('../../db-credentials');
const T = require('../T');

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

  const [transpilationError, sql] = T(() => transpile(query, order, direction, offset));
  if (transpilationError) return res.status(400).send(transpilationError);

  const client = new Client(credentials);
  const [connectionError] = await T(client.connect());
  if (connectionError) {
    client.end();
    return res.status(500).send(connectionError);
  }

  const [queryError, queryResults] = await T(client.query(sql));
  client.end();
  if (queryError) return res.status(500).send(queryError);
  const { rows: games } = queryResults;

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
};
