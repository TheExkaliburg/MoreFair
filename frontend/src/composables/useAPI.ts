import axios from "axios";
import Cookies from "js-cookie";

interface ISession {
  accessToken: string;
  refreshToken: string;
}

const isDevMode = process.env.NODE_ENV !== "production";

axios.interceptors.request.use(
  function (config) {
    if (isDevMode) {
      config.url = "http://localhost:8080" + config.url;
    }

    const tokens = Cookies.get("tokens");
    if (!tokens) return config;

    const session: ISession = JSON.parse(tokens);

    config.headers = {
      ...config.headers,
      authorization: `Bearer ${session.accessToken}`,
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
      const session: ISession = JSON.parse(Cookies.get("tokens"));
      const response = await axios.post("/api/auth/refresh", {
        refreshToken: session.refreshToken,
      });
      console.log(response);
      Cookies.set("tokens", JSON.stringify(response.data));
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
  },
};

export const useAPI = () => {
  return API;
};
