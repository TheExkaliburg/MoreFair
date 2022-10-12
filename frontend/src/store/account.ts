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
    return refreshToken.value !== "";
  });

  const isGuest = computed<boolean>(() => {
    return uuid.value !== "";
  });

  // actions
  function registerGuest() {
    if (uuid.value !== "") {
      login(uuid.value, uuid.value);
      return;
    }

    API.auth.registerGuest().then((response) => {
      // 201 - Created
      if (response.status === 201) {
        uuid.value = response.data;
        login(uuid.value, uuid.value);
      }
    });
  }

  function registerAccount(username: string, password: string) {
    // TODO
    API.auth.register(username, password, uuid.value).then((response) => {
      // 201 - Created
      console.log(response);
      if (response.status === 201) {
        alert(response.data);
      }
    });
  }

  function login(username: string, password: string) {
    console.debug("Logging in...");
    API.auth.login(username, password).then((response) => {
      // 200 - OK
      if (response.status === 200) {
        accessToken.value = response.data.accessToken;
        refreshToken.value = response.data.refreshToken;
        Cookies.set("_uuid", uuid.value, { expires: 365 });
      }
    });
  }

  function refresh() {
    console.debug("Refreshing tokens...");
    API.auth.refresh(refreshToken.value).then((response) => {
      // 200 - OK
      if (response.status === 200) {
        accessToken.value = response.data.accessToken;
        refreshToken.value = response.data.refreshToken;
      }
    });
  }

  watch(uuid, (value: string) => {
    if (value !== "") Cookies.set("_uuid", value, { expires: 365 });
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
    accessToken,
    refreshToken,
    // getters
    hasCredentials,
    isGuest,
    // actions
    login,
    registerGuest,
    registerAccount,
    refresh,
  };
});
