import axios, { AxiosError, AxiosRequestConfig } from "axios";
import { navigateTo } from "nuxt/app";
import Cookies from "js-cookie";

const isDevMode = process.env.NODE_ENV !== "production";

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
      navigateTo("/");
    }
    return Promise.reject(error);
  }
);

const API = {
  auth: {
    login: (username: string, password: string) => {
      const params = new URLSearchParams();
      params.append("username", username);
      params.append("password", password);
      params.append("remember-me", "true");
      return axiosInstance.post("/api/auth/login", params, {
        withCredentials: true,
      });
    },
    registerGuest: () => {
      return axiosInstance.post("/api/auth/register/guest");
    },
    register: (username: string, password: string, uuid?: string) => {
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
};

export const useAPI = () => {
  const xsrfToken = Cookies.get("XSRF-TOKEN");
  if (!xsrfToken) {
    API.auth.authenticationStatus().then((_) => {});
  }
  return API;
};
