const templates = require('./templates');

function search({ req, games, fnName }) {
  if (games.length === 0) return templates.end();

  const offset = parseInt(req.query.offset, 10) || 0;
  const newOffset = offset + games.length;
  const originalUrl = req.originalUrl.replace('/?', `/${fnName}?`);
  const url = `${req.protocol}://${req.hostname}${originalUrl}`;
  const nextURL = url.includes('offset=')
    ? url.replace(/offset=\d+/, `offset=${newOffset}`)
    : `${url}&offset=${newOffset}`;

  const headings = games
    .map(templates.game)
    .join('\n');

  return `
    <!DOCTYPE html>
    ${headings}
    <br>
    <p><a href="${nextURL}">Next</a></p>
  `;
}

module.exports = {
  search,
};
