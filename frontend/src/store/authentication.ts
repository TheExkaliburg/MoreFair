import { defineStore } from "pinia";
import Cookies from "js-cookie";
import { computed, ref, watch } from "vue";
import { useAPI } from "~/composables/useAPI";

export const useAuthStore = defineStore("auth", () => {
  const API = useAPI();
  // vars
  const uuid = ref<string>(Cookies.get("_uuid") || "");
  const authenticationStatus = ref<boolean>(false);

  const state = reactive({
    uuid,
    authenticationStatus,
  });

  const getters = reactive({
    isGuest: computed<boolean>(() => {
      return uuid.value !== "";
    }),
    homeLocation: computed<string>(() => {
      return authenticationStatus.value ? "/game" : "/";
    }),
  });

  API.auth
    .authenticationStatus()
    .then((response) => {
      authenticationStatus.value = Boolean(response.data);
    })
    .catch((error) => {
      if (error.status === 401) {
        authenticationStatus.value = Boolean(false);
      }
    });

  // actions
  async function registerGuest() {
    if (uuid.value !== "") {
      await login(uuid.value, uuid.value);
      return;
    }

    await API.auth.registerGuest().then(async (response) => {
      // 201 - Created
      if (response.status === 201) {
        uuid.value = response.data;
        await login(uuid.value, uuid.value);
      }
    });
  }

  async function registerAccount(username: string, password: string) {
    // TODO
    await API.auth.register(username, password, uuid.value).then((response) => {
      // 201 - Created
      if (response.status === 201) {
        alert(response.data);
      }
    });
  }

  async function login(username: string, password: string) {
    await API.auth.login(username, password).then((response) => {
      // 200 - OK
      if (response.status === 200) {
        authenticationStatus.value = true;
        if (getters.isGuest) {
          Cookies.set("_uuid", uuid.value, {
            expires: 365,
            secure: true,
            sameSite: "strict",
          });
        }
      }
    });
  }

  // Side effects
  watch(uuid, (value: string) => {
    if (value !== "")
      Cookies.set("_uuid", value, {
        expires: 365,
        secure: true,
        sameSite: "strict",
      });
    else Cookies.remove("_uuid");
  });

  return {
    state,
    getters,
    // actions
    actions: {
      login,
      registerGuest,
      registerAccount,
    },
    login,
    registerGuest,
    registerAccount,
  };
});
