import axios, { AxiosError, AxiosRequestConfig } from "axios";
import Cookies from "js-cookie";

const isDevMode = process.env.NODE_ENV !== "production";

axios.defaults.withCredentials = true;

axios.interceptors.request.use(
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

const API = {
  auth: {
    login: (username: string, password: string) => {
      const params = new URLSearchParams();
      params.append("username", username);
      params.append("password", password);
      return axios.post("/api/auth/login", params, { withCredentials: true });
    },
    registerGuest: () => {
      return axios.post("/api/auth/register/guest");
    },
    register: (username: string, password: string, uuid?: string) => {
      const params = new URLSearchParams();
      params.append("username", username);
      params.append("password", password);
      if (uuid) params.append("uuid", uuid);
      return axios.post("/api/auth/register", params);
    },
    changePassword: (oldPassword: string, newPassword: string) => {
      const params = new URLSearchParams();
      params.append("oldPassword", oldPassword);
      params.append("newPassword", newPassword);
      return axios.post("/api/auth/password/change", params);
    },
    forgotPassword: (username: string) => {
      const params = new URLSearchParams();
      params.append("username", username);
      return axios.post("/api/auth/password/forgot", params);
    },
    resetPassword: (token: string, password: string) => {
      const params = new URLSearchParams();
      params.append("token", token);
      params.append("password", password);
      return axios.post("/api/auth/password/reset", params);
    },
    logout: () => {
      return axios.post("/api/auth/logout");
    },
    authenticationStatus: () => {
      return axios.get("/api/auth");
    },
  },
  account: {
    changeDisplayName: (displayName: string) => {
      const params = new URLSearchParams();
      params.append("displayName", displayName);
      return axios.patch("/api/account/name", params);
    },
    getAccountDetails: () => {
      return axios.get("/api/account");
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
