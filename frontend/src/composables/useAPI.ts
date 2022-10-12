import axios from "axios";
import Cookies from "js-cookie";
import { useAccountStore } from "~/store/account";

const isDevMode = process.env.NODE_ENV !== "production";

axios.interceptors.request.use(
  function (config) {
    if (isDevMode) {
      config.url = "http://localhost:8080" + config.url;
    }

    const accessToken = Cookies.get("accessToken");
    if (!accessToken) return config;

    config.headers = {
      ...config.headers,
      Authorization: `Bearer ${accessToken}`,
    };

    return config;
  },
  (error) => Promise.reject(error)
);

axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    const config = error?.config;

    if (error?.response?.status === 403 && !config?.sent) {
      config.sent = true;
      const response = await API.auth.refresh(Cookies.get("refreshToken"));
      useAccountStore().accessToken = response.data.accessToken;
      useAccountStore().refreshToken = response.data.refreshToken;
      return axios(config);
    }
  }
);

const API = {
  auth: {
    login: (username: string, password: string) => {
      const params = new URLSearchParams();
      params.append("username", username);
      params.append("password", password);
      return axios.post("/api/auth/login", params);
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
      return axios.get("/api/auth/refresh", { params });
    },
  },
};

export const useAPI = () => {
  return API;
};
