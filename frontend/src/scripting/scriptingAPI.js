import store from "../store";
import { computed, readonly } from "vue";

// let user = {
//   accountId: computed(() => store.state.user.accountId),
//   highestCurrentLadder: computed(() => store.state.user.highestCurrentLadder),
//   accessRole: computed(() => store.state.user.accessRole),
// };
//

let state = readonly(store.state);

let api = readonly({
  state: computed(() => state),
  getters: store.getters,
  subscribeToHook: (id, fn) =>
    store.commit({ type: "hooks/subscribeToHook", id: id, fn: fn }),
});

function subscribe(link) {
  console.log("This is not implemented yet.", link);
  // TODO: Verify Link

  // TODO: Load Link

  // TODO: Add Link into indexedDb

  // TODO: Execute Code
  return -1;
}

function unsubscribe(link) {
  console.log("This is not implemented yet.", link);
  return -1;
}

/**
 * Takes a function that registers the script and provides it access to the data and hooks
 * @param func
 */
function register(func) {
  return func(api);
}

const Fair = {
  subscribe,
  unsubscribe,
  register,
};

export default Fair;
