export default function throttle(fn, wait) {
  let last = null;
  return async (...args) => {
    if (last === null) {
      last = Date.now();
      return fn(...args);
    }
    return new Promise((resolve) => {
      setTimeout(() => {
        last = Date.now();
        return resolve((fn(...args)));
      }, last + wait - Date.now());
    });
  };
}
