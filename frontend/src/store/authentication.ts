import { defineStore } from "pinia";
import Cookies from "js-cookie";
import { computed, ref, watch } from "vue";
import { navigateTo } from "nuxt/app";
import { useAPI } from "~/composables/useAPI";
import { useZxcvbn } from "~/composables/useZxcvbn";
import { useAccountStore } from "~/store/account";
import { useToasts } from "~/composables/useToasts";

const emailRegex =
  /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,15}$/;

export const useAuthStore = defineStore("auth", () => {
  const API = useAPI();

  const state = reactive({
    uuid: ref<string>(Cookies.get("_uuid") || ""),
    authenticationStatus: ref<boolean>(false),
  });

  const getters = reactive({
    isGuest: computed<boolean>(() => {
      return state.uuid !== "";
    }),
    homeLocation: computed<string>(() => {
      return state.authenticationStatus ? "/" : "/login";
    }),
  });

  getAuthenticationStatus().then();

  // actions
  async function getAuthenticationStatus() {
    await API.auth
      .authenticationStatus()
      .then((response) => {
        state.authenticationStatus = Boolean(response.data);
        return Promise.resolve(response);
      })
      .catch((error) => {
        if (error.status === 401) {
          state.authenticationStatus = Boolean(false);
        }
        return Promise.reject(error);
      });
  }

  async function registerGuest() {
    if (state.uuid !== "") {
      return await login(state.uuid, state.uuid, true);
    }

    return await API.auth
      .registerGuest()
      .then(async (response) => {
        // 201 - Created
        if (response.status === 201) {
          state.uuid = response.data;
          return await login(state.uuid, state.uuid, true);
        }
        return Promise.resolve(response);
      })
      .catch((err) => {
        useToasts(err.response.data.message, { type: "error" });
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
        useToasts(err.response.data.message, { type: "error" });
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
      .register(email, password, state.uuid)
      .then((res) => {
        if (res.status === 201) {
          alert(res.data.message);
        }
        return Promise.resolve(res);
      })
      .catch((err) => {
        useToasts(err.response.data.message, { type: "error" });
        return Promise.reject(err);
      });
  }

  async function login(
    username: string,
    password: string,
    rememberMe: boolean = false
  ) {
    if (state.authenticationStatus) navigateTo("/");
    return await API.auth
      .login(username, password, rememberMe)
      .then((response) => {
        // 200 - OK
        if (response.status === 200) {
          state.authenticationStatus = true;
          if (getters.isGuest) {
            Cookies.set("_uuid", state.uuid, {
              expires: 365,
              secure: true,
              sameSite: "strict",
            });
          }
        }
        navigateTo("/");
        return Promise.resolve(response);
      })
      .catch((err) => {
        if (err.response.status === 401) {
          useToasts("Invalid username or password", { type: "error" });
        } else {
          useToasts(err.response.data.message, { type: "error" });
        }
        return Promise.reject(err);
      });
  }

  async function logout() {
    return await API.auth
      .logout()
      .then((response) => {
        // 200 - OK
        if (response.status === 200) {
          state.authenticationStatus = false;
          window.location.href = "/login";
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
        useToasts(err.response.data.message, { type: "error" });
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
        useToasts(err.response.data.message, { type: "error" });
        return Promise.reject(err);
      });
  }

  async function changeEmail(email: string) {
    if (!checkEmail(email)) return Promise.reject(new Error("Invalid email"));

    return await API.auth
      .requestEmailChange(email)
      .then((res) => Promise.resolve(res))
      .catch((err) => {
        useToasts(err.response.data.message, { type: "error" });
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
        useToasts(err.response.data.message, { type: "error" });
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
        useToasts(err.response.data.message, { type: "error" });
        return Promise.reject(err);
      });
  }

  // Side effects
  watch(
    () => state.uuid,
    (value: string) => {
      if (value !== "")
        Cookies.set("_uuid", value, {
          expires: 365,
          secure: true,
          sameSite: "strict",
        });
      else Cookies.remove("_uuid");
    }
  );

  setInterval(() => {
    const uuidCookie = Cookies.get("_uuid") || undefined;
    if (uuidCookie === undefined) {
      state.uuid = "";
      return;
    }
    if (uuidCookie !== state.uuid) state.uuid = uuidCookie;
  }, 1000);

  return {
    state,
    getters,
    // actions
    actions: {
      getAuthenticationStatus,
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
  };
});

function checkPassword(password: string): boolean {
  if (password.length > 64) {
    useToasts("Password can only be 64 characters long", { type: "error" });
    return false;
  }
  if (password.length < 8) {
    useToasts("Password needs to be at least 8 characters long", {
      type: "error",
    });
    return false;
  }

  const zxcvbn = useZxcvbn(password);

  if (zxcvbn.value.score < 3) {
    useToasts(
      `Password is too weak.\n\n${zxcvbn.value.feedback.warning}\n${zxcvbn.value.feedback.suggestions}`,
      { type: "error" }
    );
    return false;
  }

  return true;
}

function checkEmail(email: string): boolean {
  if (email.length > 254) {
    useToasts("Email needs to be less than 254 characters long", {
      type: "error",
    });
    return false;
  }

  // Check with regex if email is valid
  if (!emailRegex.test(email)) {
    useToasts("Email needs to be a valid email address", {
      type: "error",
    });
    return false;
  }
  return true;
}
