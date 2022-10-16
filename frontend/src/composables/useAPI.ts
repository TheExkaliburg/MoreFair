import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from "axios";
import Cookies from "js-cookie";
import { useAccountStore } from "~/store/account";

const isDevMode = process.env.NODE_ENV !== "production";

axios.interceptors.request.use(
  function (config: AxiosRequestConfig) {
    if (isDevMode) {
      config.baseURL = "http://localhost:8080";
    }

    const accessToken = Cookies.get("accessToken");
    if (!accessToken) return config;

    config.headers = {
      ...config.headers,
      Authorization: `Bearer ${accessToken}`,
    };

    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

axios.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error: AxiosError) => {
    const config = error?.config;
    const url = config?.url;

    if (error?.response?.status === 403 && !url.endsWith("/api/auth/refresh")) {
      const response = await API.auth.refresh(Cookies.get("refreshToken"));

      if (response.status < 300) {
        useAccountStore().accessToken = response.data.accessToken;
        useAccountStore().refreshToken = response.data.refreshToken;
        return axios(config);
      }
    }
    console.log(error);
    return Promise.reject(error);
  }
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
    refresh: (refreshToken: string) => {
      const params = new URLSearchParams();
      params.append("refreshToken", refreshToken);
      return axios.get("/api/auth/refresh", { params, withCredentials: true });
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
  return API;
};
