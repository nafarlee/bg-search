const { map } = require('lodash');

exports.game = function game({
  average_rating: ar,
  rating_votes: rv,
  average_weight: aw,
  weight_votes: wv,
  minimum_players: minPlay,
  maximum_players: maxPlay,
  minimum_playtime: minTime,
  maximum_playtime: maxTime,
  primary_name: name,
  id,
  year,
}) {
  const sameOrRange = (a, b) => (a === b ? a : `${a} - ${b}`);
  return `
    <div style="border-left: 2px solid black; padding-left: 1em;">
      <h2><a href="https://boardgamegeek.com/boardgame/${id}">
          ${name} (${year})
        </a>
      </h2>
      <p>Rating: ${ar} with ${rv} votes</p>
      <p>Weight: ${aw} with ${wv} votes</p>
      <p>Players: ${sameOrRange(minPlay, maxPlay)}</p>
      <p>Playtime: ${sameOrRange(minTime, maxTime)}</p>
    </div>
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
  query = '',
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
