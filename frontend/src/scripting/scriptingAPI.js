import { reactive, readonly } from "vue";
import store from "../store";

window.state = store.state;

let user = {
  accountId: store.state.user.accountId,
  accessRole: store.state.user.accessRole,
  highestCurrentLadder: store.state.user.highestCurrentLadder,
};

let state = readonly({
  ladder: store.state.ladder,
  chat: store.state.chat,
  numberFormatter: store.state.numberFormatter,
  settings: store.state.settings,
  options: store.state.options,
  user: reactive(user),
});

let api = {
  state: state,
  addToHook: (id, fn) =>
    store.commit({ type: "hooks/addToHook", id: id, fn: fn }),
};

function subscribe(link) {
  console.log(link);

  // TODO: Verify Link

  // TODO: Load Link

  // TODO: Add Link into indexedDb

  // TODO: Execute Code
}

function unsubscribe(link) {
  console.log(link);
}

/**
 * Takes a function that registers the script and provides it access to the data and hooks
 * @param func
 */
function register(func) {
  func(api);
}

const Fair = {
  subscribe,
  unsubscribe,
  register,
};

export default Fair;
