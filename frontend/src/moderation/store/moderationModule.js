import axios from "axios";

const searchUser =
  process.env.NODE_ENV === "production"
    ? "/mod/search/user"
    : "http://localhost:8080/mod/search/user";

const moderationModule = {
  namespaced: true,
  state: () => {
    return {
      userNameSearchResults: "",
    };
  },
  mutations: {
    fillSearchResults(state, { response }) {
      state.userNameSearchResults = response;
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
  },
  getters: {},
};

export default moderationModule;
