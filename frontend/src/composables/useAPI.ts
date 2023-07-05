import axios, { AxiosError, AxiosRequestConfig } from "axios";
import { navigateTo } from "nuxt/app";
import Cookies from "js-cookie";
import { useAuthStore } from "~/store/authentication";
import { useToasts } from "~/composables/useToasts";

const isDevMode = process.env.NODE_ENV !== "production";
let lastXsrfToken = Cookies.get("XSRF-TOKEN");

export const axiosInstance = axios.create({
  baseURL: isDevMode ? "http://localhost:8080" : "",
});

axiosInstance.defaults.withCredentials = true;

axiosInstance.interceptors.request.use(
  function (config: AxiosRequestConfig) {
    if (isDevMode) {
      config.baseURL = "http://localhost:8080";
    }

    const xsrfToken = Cookies.get("XSRF-TOKEN");
    lastXsrfToken = xsrfToken;

    if (!xsrfToken) return config;

    config.headers = {
      ...config.headers,
      "X-XSRF-TOKEN": xsrfToken,
    };

    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

axiosInstance.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      useAuthStore().state.authenticationStatus = false;
      useAuthStore().state.uuid = Cookies.get("_uuid") || "";
      navigateTo("/login");
    }

    // if status is 502, the server is down
    if (error.response?.status === 502) {
      useToasts("Server is down", { type: "error" });
    }

    if (error.response?.status === 403) {
      const xsrfCookie = Cookies.get("XSRF-TOKEN");
      if (xsrfCookie === lastXsrfToken) {
        return Promise.reject(error);
      }

      const headers = error.config?.headers;
      if (headers === undefined) return Promise.reject(error);
      const isRetry = Boolean(headers["X-RETRY"]);
      if (!isRetry) {
        const xsrfToken = Cookies.get("XSRF-TOKEN");
        if (xsrfToken) {
          const config = error.config;
          config.headers = {
            ...config.headers,
            "X-RETRY": true,
          };
          return axiosInstance.request(config);
        }
      }
    }
    return Promise.reject(error);
  }
);

const API = {
  auth: {
    login: (username: string, password: string, rememberMe = false) => {
      username = username.toLowerCase();
      const params = new URLSearchParams();
      params.append("username", username);
      params.append("password", password);
      if (rememberMe) params.append("remember-me", "true");
      return axiosInstance.post("/api/auth/login", params, {
        withCredentials: true,
      });
    },
    registerGuest: () => {
      return axiosInstance.post("/api/auth/register/guest");
    },
    register: (username: string, password: string, uuid?: string) => {
      username = username.toLowerCase();
      const params = new URLSearchParams();
      params.append("username", username);
      params.append("password", password);
      if (uuid) params.append("uuid", uuid);
      return axiosInstance.post("/api/auth/register", params);
    },
    changePassword: (oldPassword: string, newPassword: string) => {
      const params = new URLSearchParams();
      params.append("oldPassword", oldPassword);
      params.append("newPassword", newPassword);
      return axiosInstance.post("/api/auth/password/change", params);
    },
    forgotPassword: (username: string) => {
      username = username.toLowerCase();
      const params = new URLSearchParams();
      params.append("username", username);
      return axiosInstance.post("/api/auth/password/forgot", params);
    },
    resetPassword: (token: string, password: string) => {
      const params = new URLSearchParams();
      params.append("token", token);
      params.append("password", password);
      return axiosInstance.post("/api/auth/password/reset", params);
    },
    requestEmailChange: (email: string) => {
      email = email.toLowerCase();
      const params = new URLSearchParams();
      params.append("email", email);
      return axiosInstance.patch("/api/auth/email", params);
    },
    confirmEmailChange: (token: string) => {
      const params = new URLSearchParams();
      params.append("token", token);
      return axiosInstance.post("/api/auth/email", params);
    },
    logout: () => {
      return axiosInstance.post("/api/auth/logout");
    },
    authenticationStatus: () => {
      return axiosInstance.get("/api/auth");
    },
  },
  account: {
    changeDisplayName: (displayName: string) => {
      const params = new URLSearchParams();
      params.append("displayName", displayName);
      return axiosInstance.patch("/api/account/name", params);
    },

    getAccountDetails: () => {
      return axiosInstance.get("/api/account");
    },
  },
  ladder: {
    getLadder: (number: number) => {
      const params = new URLSearchParams();
      params.append("number", number.toString());
      return axiosInstance.get("/api/ladder", { params });
    },
  },
  chat: {
    getChat: (number: number) => {
      const params = new URLSearchParams();
      params.append("number", number.toString());
      return axiosInstance.get("/api/chat", { params });
    },
  },
  round: {
    getCurrentRound: () => {
      return axiosInstance.get("/api/round");
    },
  },
  moderation: {
    getChatLog: () => {
      return axiosInstance.get("/api/moderation/chat");
    },
    searchUsername(username: string) {
      const params = new URLSearchParams();
      params.append("username", username);
      return axiosInstance.get("/api/moderation/search/user", { params });
    },
    searchAltAccouunts(accountId: number) {
      const params = new URLSearchParams();
      params.append("accountId", accountId.toString());
      return axiosInstance.get("/api/moderation/search/alts", { params });
    },
  },
};

let isInitialized = false;
if (!isInitialized) {
  API.auth.authenticationStatus().then((_) => {
    isInitialized = true;
  });
}

export const useAPI = () => {
  return API;
};
