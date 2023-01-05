import axios from "axios";

const searchUser =
  process.env.NODE_ENV === "production"
    ? "/mod/search/user"
    : "http://localhost:8080/mod/search/user";
const searchAlts =
  process.env.NODE_ENV === "production"
    ? "/mod/search/alts"
    : "http://localhost:8080/mod/search/alts";

const moderationModule = {
  namespaced: true,
  state: () => {
    return {
      userNameSearchResults: "",
      altSearchResults: "",
    };
  },
  mutations: {
    fillSearchResults(state, { response }) {
      state.userNameSearchResults = response;
    },
    fillAltSearchResults(state, { response }) {
      state.altSearchResults = response;
    },
  },
  actions: {
    searchName({ commit }, { name }) {
      axios
        .get(searchUser, {
          params: {
            name: name,
          },
          withCredentials: true,
        })
        .then((res) => {
          commit({ type: "fillSearchResults", response: res.data });
        })
        .catch((err) => {
          console.error(err);
          commit({ type: "fillSearchResults", response: err });
        });
    },
    searchAlts({ commit }, { id }) {
      axios
        .get(searchAlts, {
          params: {
            id: id,
          },
          withCredentials: true,
        })
        .then((res) => {
          commit({ type: "fillAltSearchResults", response: res.data });
        })
        .catch((err) => {
          console.error(err);
          commit({ type: "fillAltSearchResults", response: err });
        });
    },
  },
  getters: {},
};

export default moderationModule;
