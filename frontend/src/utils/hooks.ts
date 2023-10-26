import { useLadderStore } from "~/store/ladder";
import { useRoundStore } from "~/store/round";
import { useChatStore } from "~/store/chat";
import { useOptionsStore } from "~/store/options";
import { useAccountStore } from "~/store/account";
import { useUiStore } from "~/store/ui";
import { StompCallback, useStomp } from "~/composables/useStomp";

const api = {
  stores: {
    useLadderStore: () => unpackStore(useLadderStore()),
    useRoundStore: () => unpackStore(useRoundStore()),
    useChatStore: () => unpackStore(useChatStore()),
    useOptionsStore: () => unpackStore(useOptionsStore()),
    useAccountStore: () => unpackStore(useAccountStore()),
    useUiStore: () => unpackStore(useUiStore()),
  },
  getHooks: () => useStomp().callbacks,
  addCallback: (
    hook: StompCallback<unknown>[],
    identifier: string,
    callback: (body: unknown) => void,
  ) => useStomp().addCallback(hook, identifier, callback),
};

function unpackStore(store: any) {
  const { state, getters, actions } = store;
  return { state, getters, actions };
}

function register(func: (api: any) => void) {
  func(api);
}

const Fair = {
  register,
};

export default Fair;

declare global {
  interface Window {
    Fair: typeof Fair;
  }
}
