function includes(str, substr) {
  return str
    .toLowercase()
    .includes(substr.toLowerCase());
}

function NAME(term, game) {
  return game['primary-name']
    .toLowerCase()
    .includes(term.value.toLowerCase());
}

module.exports = {
  NAME,
};
