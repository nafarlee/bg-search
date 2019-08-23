const templates = require('./templates');

exports.search = function search({ req, games, fnName }) {
  if (games.length === 0) return templates.end();

  const offset = parseInt(req.query.offset, 10) || 0;
  const newOffset = offset + games.length;
  const originalUrl = req.originalUrl.replace('/?', `/${fnName}?`);
  const url = `${req.protocol}://${req.hostname}${originalUrl}`;
  const nextURL = url.includes('offset=')
    ? url.replace(/offset=\d+/, `offset=${newOffset}`)
    : `${url}&offset=${newOffset}`;

  return templates.search({
    games,
    nextURL,
    query: req.query.query,
    order: req.query.order,
    direction: req.query.direction,
  });
};
