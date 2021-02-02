export default (risk) => {
  if (risk instanceof Function) {
    try {
      return [null, risk()];
    } catch (error) {
      return [error, null];
    }
  }

  if (risk instanceof Promise) {
    return risk
      .then((x) => [null, x])
      .catch((error) => [error, null]);
  }

  throw new Error('Unsupported type passed to "T"');
};
