import { defineStore } from "pinia";
import Cookies from "js-cookie";
import { watch } from "vue";

export const useAccountStore = defineStore("account", () => {
  const API = useAPI();

  // vars
  const uuid = ref<string>(Cookies.get("_uuid") || "");
  const authenticationStatus = ref<boolean>(false);

  API.auth.authenticationStatus().then((response) => {
    authenticationStatus.value = Boolean(response.data);
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
    API.auth.login(username, password).then((response) => {
      // 200 - OK
      if (response.status === 200) {
        authenticationStatus.value = true;
        if (isGuest.value) {
          Cookies.set("_uuid", uuid.value, {
            expires: 365,
            secure: true,
            sameSite: "strict",
          });
        }
      }
    });
  }

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
    // vars
    uuid,
    authenticationStatus,
    // getters
    isGuest,
    // actions
    login,
    registerGuest,
    registerAccount,
  };
});
