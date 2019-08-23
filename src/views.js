const { format } = require('url');

const { renderFile } = require('pug');

exports.search = function search({ req, games }) {
  if (games.length === 0) return renderFile('src/templates/empty.pug');

  const nextURL = format({
    protocol: req.protocol,
    host: req.get('host'),
    pathname: req.path,
    query: {
      offset: parseInt(req.query.offset, 10) || 0 + games.length,
      ...req.query,
    },
  });

  return renderFile('src/templates/search.pug', {
    games,
    nextURL,
    query: req.query.query,
    order: req.query.order,
    direction: req.query.direction,
  });
};
