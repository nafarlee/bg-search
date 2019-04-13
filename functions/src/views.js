function search({ req, games }) {
  if (games.length === 0) return '<!DOCTYPE html>\n<h1>No more results!</h1>';

  const nextID = games[games.length - 1].id;
  const url = `${req.protocol}://${req.hostname}${req.originalUrl}`;
  const nextURL = url.includes('checkpoint=')
    ? url.replace(/checkpoint=\d+/, `checkpoint=${nextID}`)
    : `${url}&checkpoint=${nextID}`;

  const headings = games
    .map(({ 'primary-name': name, id, year }) => (
      `<h1><a href="https://boardgamegeek.com/boardgame/${id}">${name} (${year})</a></h1>`
    ))
    .join('\n');
  return `
<!DOCTYPE html>
${headings}
<br>
<p><a href="${nextURL}">Next</a></p>`;
}

module.exports = {
  search,
};
