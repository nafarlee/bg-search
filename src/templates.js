const { map } = require('lodash');

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

exports.dropdown = function dropdown(name, configuration, selected) {
  const toOption = (text, value) => `
    <option value="${value}" ${value === selected ? 'selected' : ''}>
      ${text}
    </option>
  `;
  return `
    <select name="${name}">
      ${map(configuration, toOption).join('\n')}
    </select>
  `;
};

exports.searchBox = function searchBox({
  query,
  order,
  direction,
}) {
  const orders = {
    id: 'ID',
    primary_name: 'Name',
    rating_votes: 'Number of Ratings',
    average_rating: 'Average Rating',
    bayes_rating: 'Geek Rating',
    rating_deviation: 'Rating Deviation',
    average_weight: 'Weight',
    weight_votes: 'Number of Weight Ratings',
    year: 'Release Year',
    age: 'Minimum Age',
    minimum_players: 'Minimum Players',
    maximum_players: 'Maximum Players',
    minimum_playtime: 'Minimum Playtime',
    maximum_playtime: 'Maximum Playtime',
  };
  const directions = {
    DESC: 'Descending',
    ASC: 'Ascending',
  };
  return `
      <form
          style="display: flex; align-items: baseline;"
          method="get"
          action="/search">
        <input
            style="flex-grow: 1"
            spellcheck="false"
            autocomplete="off"
            autocapitalize="off"
            autocorrect="off"
            class="search"
            value="${query.replace('"', '&quot;')}"
            type="search"
            mozactionhint="search"
            name="query">
        </input>
        <div class="sort" style="margin: 1ex;">
          <label for="order">Sort</label>
          ${exports.dropdown('order', orders, order)}
        </div>
        <div class="direction">
          <label for="direction">Direction</label>
          ${exports.dropdown('direction', directions, direction)}
        </div>
      </form>
  `;
};

exports.search = function search({
  games,
  nextURL,
  query,
  order,
  direction,
}) {
  return `
    <!DOCTYPE html>
    ${exports.searchBox({ query, order, direction })}
    ${exports.games(games)}
    <br>
    <p>
      <a href="${nextURL}">
        Next
      </a>
    </p>
  `;
};
