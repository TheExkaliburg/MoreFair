import Ranker from "@/ladder/entities/ranker";
import ladderActions from "@/store/modules/ladder/ladderActions";
import statsModule from "@/store/modules/ladder/stats/statsModule";
import ladderMutations from "@/store/modules/ladder/ladderMutations";
import ladderGetters from "@/store/modules/ladder/ladderGetters";
import Decimal from "break_infinity.js";

export default {
  namespaced: true,
  state: () => {
    return {
      number: 1,
      rankers: [new Ranker()],
      yourRanker: new Ranker(),
      types: ["DEFAULT"],
      basePointsToPromote: new Decimal(Infinity),
    };
  },
  mutations: ladderMutations,
  actions: ladderActions,
  getters: ladderGetters,
  modules: {
    stats: statsModule,
  },
};
