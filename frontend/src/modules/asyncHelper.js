export function getWaiter(ms) {
  let timestamp = Date.now();
  return async function () {
    if (timestamp + ms < Date.now()) {
      return;
    }
    timestamp = Date.now();
    return new Promise((resolve) => {
      setTimeout(resolve, 0);
    });
  };
}
