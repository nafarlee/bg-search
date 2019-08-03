function search({ req, games, fnName }) {
  if (games.length === 0) return '<!DOCTYPE html>\n<h1>No more results!</h1>';

  const offset = parseInt(req.query.offset, 10) || 0;
  const newOffset = offset + games.length;
  const originalUrl = req.originalUrl.replace('/?', `/${fnName}?`);
  const url = `${req.protocol}://${req.hostname}${originalUrl}`;
  const nextURL = url.includes('offset=')
    ? url.replace(/offset=\d+/, `offset=${newOffset}`)
    : `${url}&offset=${newOffset}`;

  const headings = games
    .map(({ thumbnail, primary_name: name, id, year }) => (`
      <section>
        <img src="${thumbnail}" />
        <h2><a href="https://boardgamegeek.com/boardgame/${id}">${name} (${year})</a></h2>
      </section>
    `))
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
