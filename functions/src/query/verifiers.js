function NAME(term, game) {
  return game['primary-name']
    .toLowerCase()
    .includes(term.value.toLowerCase());
}

module.exports = {
  NAME,
};
