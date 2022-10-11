import { defineStore } from "pinia";
import Cookies from "js-cookie";
import { watch } from "vue";

export const useAccountStore = defineStore("account", () => {
  // vars
  const uuid = ref<string>(Cookies.get("_uuid") || "");
  const accessToken = ref<string>(Cookies.get("accessToken") || "");
  const refreshToken = ref<string>(Cookies.get("refreshToken") || "");

  const API = useAPI();

  // getter
  const hasCredentials = computed<boolean>(() => {
    return refreshToken.value !== "" || uuid.value !== "";
  });

  // actions
  function registerGuest() {
    if (hasCredentials.value) return;

    API.auth.registerGuest().then((response) => {
      if (response.status === 201) {
        uuid.value = response.data.uuid;
      }
    });
  }

  function login() {
    API.auth.login(uuid.value, uuid.value).then((response) => {
      console.log("login", response);
    });
  }

  watch(uuid, (value: string) => {
    if (value !== "") Cookies.set("_uuid", value);
    else Cookies.remove("_uuid");
  });

  watch(accessToken, (value: string) => {
    if (value !== "") Cookies.set("accessToken", value, { expires: 1 });
    else Cookies.remove("accessToken");
  });

  watch(refreshToken, (value: string) => {
    if (value !== "") Cookies.set("refreshToken", value, { expires: 30 });
    else Cookies.remove("refreshToken");
  });

  return {
    // vars
    uuid,
    // getters
    hasCredentials,
    // actions
    login,
    registerGuest,
  };
});
