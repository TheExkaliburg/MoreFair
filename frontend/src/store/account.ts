import { defineStore } from "pinia";
import Cookies from "js-cookie";
import { watch } from "vue";

export const useAccountStore = defineStore("account", () => {
  // vars
  const guest = ref<boolean>(Boolean(Cookies.get("guest")));
  const uuid = ref<string>(Cookies.get("_uuid") || "");
  const accessToken = ref<string>(Cookies.get("accessToken") || "");
  const refreshToken = ref<string>(Cookies.get("refreshToken") || "");

  const API = useAPI();

  // actions
  function registerGuest() {
    API.auth.registerGuest().then((response) => {
      console.log("registerGuest", response);
    });
  }

  function login() {
    API.auth.login(uuid.value, uuid.value).then((response) => {
      console.log("login", response);
    });
  }

  // side-effects
  watch(guest, (value: boolean) => {
    Cookies.set("guest", String(value), { expires: 365 });
  });

  watch(accessToken, (value: string) => {
    Cookies.set("accessToken", value, { expires: 1 });
  });

  watch(refreshToken, (value: string) => {
    Cookies.set("refreshToken", value, { expires: 30 });
  });

  return {
    // vars
    guest,
    uuid,
    // getters
    isLoggedIn,
    // actions
    login,
    registerGuest,
  };
});
