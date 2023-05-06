import { defineStore } from "pinia";
import Cookies from "js-cookie";
import { computed, ref, watch } from "vue";
import { navigateTo } from "nuxt/app";
import { useAPI } from "~/composables/useAPI";
import { useZxcvbn } from "~/composables/useZxcvbn";
import { useAccountStore } from "~/store/account";

const emailRegex =
  /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,15}$/;

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

  getAuthenticationStatus().then();

  // actions
  async function getAuthenticationStatus() {
    await API.auth
      .authenticationStatus()
      .then((response) => {
        authenticationStatus.value = Boolean(response.data);
        return Promise.resolve(response);
      })
      .catch((error) => {
        if (error.status === 401) {
          authenticationStatus.value = Boolean(false);
        }
        return Promise.reject(error);
      });
  }

  async function registerGuest() {
    if (uuid.value !== "") {
      return await login(uuid.value, uuid.value, true);
    }

    return await API.auth
      .registerGuest()
      .then(async (response) => {
        // 201 - Created
        if (response.status === 201) {
          uuid.value = response.data;
          return await login(uuid.value, uuid.value, true);
        }
        return Promise.resolve(response);
      })
      .catch((err) => {
        alert(err.response.data.message);
        return Promise.reject(err);
      });
  }

  async function registerAccount(email: string, password: string) {
    if (getters.isGuest) return await upgradeGuest(email, password);

    if (!checkEmail(email)) return Promise.reject(new Error("Invalid email"));
    if (!checkPassword(password))
      return Promise.reject(new Error("Invalid password"));

    return await API.auth
      .register(email, password)
      .then((response) => {
        // 201 - Created
        if (response.status === 201) {
          alert(response.data.message);
        }
        return Promise.resolve(response);
      })
      .catch((err) => {
        alert(err.response.data.message);
        return Promise.reject(err);
      });
  }

  async function upgradeGuest(email: string, password: string) {
    if (!checkEmail(email)) return Promise.reject(new Error("Invalid email"));
    if (!checkPassword(password))
      return Promise.reject(new Error("Invalid password"));

    if (!getters.isGuest)
      return Promise.reject(new Error("Already using an upgraded account"));

    return await API.auth
      .register(email, password, uuid.value)
      .then((res) => {
        if (res.status === 201) {
          alert(res.data.message);
        }
        return Promise.resolve(res);
      })
      .catch((err) => {
        alert(err.response.data.message);
        return Promise.reject(err);
      });
  }

  async function login(
    username: string,
    password: string,
    rememberMe: boolean = false
  ) {
    if (state.authenticationStatus) navigateTo("/game");

    return await API.auth
      .login(username, password, rememberMe)
      .then((response) => {
        // 200 - OK
        console.log(response);
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
        navigateTo("/game");
        return Promise.resolve(response);
      })
      .catch((err) => {
        alert(err.response.data.message);
        return Promise.reject(err);
      });
  }

  async function logout() {
    return await API.auth
      .logout()
      .then((response) => {
        // 200 - OK
        if (response.status === 200) {
          authenticationStatus.value = false;
          window.location.href = "/";
        }
        return Promise.resolve(response);
      })
      .catch((err) => {
        return Promise.reject(err);
      });
  }

  async function forgotPassword(email: string) {
    if (!checkEmail(email)) return Promise.reject(new Error("Invalid email"));

    return await API.auth
      .forgotPassword(email)
      .then((res) => Promise.resolve(res))
      .catch((err) => {
        alert(err.response.data.message);
        return Promise.reject(err);
      });
  }

  async function resetPassword(token: string, password: string) {
    if (!checkPassword(password))
      return Promise.reject(new Error("Invalid password"));

    return await API.auth
      .resetPassword(token, password)
      .then((res) => {
        return Promise.resolve(res);
      })
      .catch((err) => {
        alert(err.response.data.message);
        return Promise.reject(err);
      });
  }

  async function changeEmail(email: string) {
    if (!checkEmail(email)) return Promise.reject(new Error("Invalid email"));

    return await API.auth
      .requestEmailChange(email)
      .then((res) => Promise.resolve(res))
      .catch((err) => {
        alert(err.response.data.message);
        return Promise.reject(err);
      });
  }

  async function confirmEmailChange(token: string) {
    return await API.auth
      .confirmEmailChange(token)
      .then((res) => {
        useAccountStore().state.email = res.data.email;
        return Promise.resolve(res);
      })
      .catch((err) => {
        alert(err.response.data.message);
        return Promise.reject(err);
      });
  }

  async function changePassword(oldPassword: string, newPassword: string) {
    if (!checkPassword(newPassword))
      return Promise.reject(new Error("Invalid password"));

    return await API.auth
      .changePassword(oldPassword, newPassword)
      .then((res) => Promise.resolve(res))
      .catch((err) => {
        alert(err.response.data.message);
        return Promise.reject(err);
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
      logout,
      registerGuest,
      registerAccount,
      upgradeGuest,
      changePassword,
      changeEmail,
      confirmEmailChange,
      forgotPassword,
      resetPassword,
    },
    login,
    registerGuest,
    registerAccount,
  };
});

function checkPassword(password: string): boolean {
  if (password.length > 64) {
    alert("Password can only be 64 characters long");
    return false;
  }
  if (password.length < 8) {
    alert("Password needs to be at least 8 characters long");
    return false;
  }

  const zxcvbn = useZxcvbn(password);

  if (zxcvbn.value.score < 3) {
    alert(
      `Password is too weak.\n\n${zxcvbn.value.feedback.warning}\n${zxcvbn.value.feedback.suggestions}`
    );
    return false;
  }

  return true;
}

function checkEmail(email: string): boolean {
  if (email.length > 254) {
    alert("Email needs to be less than 254 characters long");
    return false;
  }

  // Check with regex if email is valid
  if (!emailRegex.test(email)) {
    alert("Email needs to be a valid email address");
    return false;
  }
  return true;
}
