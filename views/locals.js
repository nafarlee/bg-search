exports.rangeToText = (text) => {
  const one = /\[(\d+),\d+\)/;
  if (one.test(text)) return text.match(one)[1];
  const many = /\[(\d+),\)/;
  if (many.test(text)) return `${text.match(many)[1]}+`;
  return text;
  throw new Error('Could not parse player recommendation range');
};
