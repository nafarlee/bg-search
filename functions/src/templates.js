exports.end = function end() {
  return `
    <!DOCTYPE html>
    <h1>No more results!</h1>
  `;
};

exports.game = function game({ primary_name: name, id, year }) {
  return `
    <h2>
      <a href="https://boardgamegeek.com/boardgame/${id}">
        ${name} (${year})
      </a>
    </h2>
  `;
};

exports.games = function games(gs) {
  return gs
    .map(exports.game)
    .join('\n');
};
