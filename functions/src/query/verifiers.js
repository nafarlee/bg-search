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

function CATEGORY(term, game) {
  return any(game.categories, c => includes(c, term.value));
}

function DESCRIPTION(term, game) {
  return includes(game.description, term.value);
}

function FAMILY(term, game) {
  return any(game.families, f => includes(f, term.value));
}

function MECHANIC(term, game) {
  return any(game.mechanics, m => includes(m, term.value));
}

function PUBLISHER(term, game) {
  return any(game.publishers, p => includes(p, term.value));
}

function DESIGNER(term, game) {
  return any(game.designers, d => includes(d, term.value));
}

module.exports = {
  ARTIST,
  CATEGORY,
  DESCRIPTION,
  DESIGNER,
  FAMILY,
  MECHANIC,
  PUBLISHER,
  NAME,
};
