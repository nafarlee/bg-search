function includes(str, substr) {
  return str
    .toLowercase()
    .includes(substr.toLowerCase());
}

function NAME(term, game) {
  return includes(game['primary-name'], term.value);
}

module.exports = {
  NAME,
};
