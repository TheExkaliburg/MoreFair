import { defineStore } from "pinia";
import { computed, ComputedRef } from "vue";
import { useAPI } from "~/composables/useAPI";
import { OnUserEventBody, useStomp } from "~/composables/useStomp";
import { AccessRole } from "~/store/account";

export type User = {
  accountId: number;
  displayName: string;
  tag: string;
  accessRole: AccessRole;
};

export type UserStoreData = {
  users: Map<number, User>;
};

export type UserRequestData = {
  users: User[];
};

export const useUserStore = defineStore("user", () => {
  const api = useAPI();
  const stomp = useStomp();

  const isInitialized = ref<boolean>(false);
  const state = reactive<UserStoreData>({
    users: new Map(),
  });

  const userComputedGetters = new Map<number, ComputedRef<User | undefined>>();
  const getters = {
    getUser: (id: number) => {
      let userComputedGetter = userComputedGetters.get(id);
      if (!userComputedGetter) {
        userComputedGetter = computed(() => {
          const result = state.users.get(id);
          // Trigger a singular fetch if user is unknown
          if (isInitialized.value && !result) {
            fetchUser(id).then();
          }
          return result;
        });
        userComputedGetters.set(id, userComputedGetter);
      }
      return userComputedGetter;
    },
  };

  init().then();

  async function init() {
    if (isInitialized.value) return Promise.resolve();
    await fetchUsers();
  }

  async function fetchUsers() {
    return await api.user.getActiveUsers().then((res) => {
      const data: UserRequestData = res.data;

      state.users = new Map();
      data.users.forEach((user) => {
        state.users.set(user.accountId, user);
      });

      stomp.addCallback(
        stomp.callbacks.onUserEvent,
        "fair_user_events",
        handleUserEvents,
      );

      isInitialized.value = true;
      return Promise.resolve(state);
    });
  }

  async function fetchUser(id: number) {
    return await api.user.getUser(id).then((res) => {
      const data: User = res.data;

      state.users.set(data.accountId, data);
      return Promise.resolve(data);
    });
  }

  function handleUserEvents(body: OnUserEventBody) {
    const newUser = body.data;
    state.users.set(newUser.accountId, newUser);
  }

  return {
    state,
    getters,
    actions: {
      init,
    },
  };
});
