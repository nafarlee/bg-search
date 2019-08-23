const { renderFile } = require('pug');

exports.search = function search({ req, games, fnName }) {
  if (games.length === 0) return renderFile('templates/empty.pug');

  const offset = parseInt(req.query.offset, 10) || 0;
  const newOffset = offset + games.length;
  const originalUrl = req.originalUrl.replace('/?', `/${fnName}?`);
  const url = `${req.protocol}://${req.hostname}${originalUrl}`;
  const nextURL = url.includes('offset=')
    ? url.replace(/offset=\d+/, `offset=${newOffset}`)
    : `${url}&offset=${newOffset}`;

  return renderFile('templates/search.pug', {
    games,
    nextURL,
    query: req.query.query,
    order: req.query.order,
    direction: req.query.direction,
  });
};
