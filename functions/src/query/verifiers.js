function includes(str, substr) {
  return str
    .toLowercase()
    .includes(substr.toLowerCase());
}

function any(xs, pred) {
  for (const x of xs) {
    if (pred(x)) return true;
  }
  return false;
}

function NAME(term, game) {
  return includes(game['primary-name'], term.value);
}

function ARTIST(term, game) {
  return any(game.artists, a => includes(a, term.value));
}

module.exports = {
  ARTIST,
  NAME,
};
