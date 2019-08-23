const { format } = require('url');

const { renderFile } = require('pug');

exports.search = function search({ req, games }) {
  const nextURL = format({
    protocol: req.protocol,
    host: req.get('host'),
    pathname: req.path,
    query: {
      ...req.query,
      offset: (parseInt(req.query.offset, 10) || 0) + games.length,
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
