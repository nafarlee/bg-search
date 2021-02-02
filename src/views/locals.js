export const rangeToText = (text) => {
  const one = /\[(\d+),\d+\)/;
  if (one.test(text)) return text.match(one)[1];
  const many = /\[(\d+),\)/;
  if (many.test(text)) return `${text.match(many)[1]}+`;
  return text;
  throw new Error('Could not parse player recommendation range');
};

export const percentageOf = (num, denom, decimals = 1) => `${(num / denom * 100).toFixed(decimals)}%`;

export const sortByProperty = (objects, field) => {
  objects.sort((a, b) => {
    const af = a[field];
    const bf = b[field];
    if (af < bf) return -1;
    if (af > bf) return 1;
    return 0;
  });
  return objects;
};
