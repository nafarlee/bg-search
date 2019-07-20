function search({ req, games, fnName }) {
  if (games.length === 0) return '<!DOCTYPE html>\n<h1>No more results!</h1>';

  const offset = req.query.offset || 0;
  const newOffset = offset + games.length;
  const originalUrl = req.originalUrl.replace('/?', `/${fnName}?`);
  const url = `${req.protocol}://${req.hostname}${originalUrl}`;
  const nextURL = url.includes('offset=')
    ? url.replace(/offset=\d+/, `offset=${newOffset}`)
    : `${url}&offset=${newOffset}`;

  const headings = games
    .map(({ primary_name: name, id, year }) => (
      `<h2><a href="https://boardgamegeek.com/boardgame/${id}">${name} (${year})</a></h2>`
    ))
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
