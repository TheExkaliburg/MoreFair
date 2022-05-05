import Version from "@/versioning/entities/version";

const versioningModule = {
  namespaced: true,
  state: () => {
    return {
      versions: [
        new Version("MAJOR", "test", {
          features: ["Versioning"],
          balancing: ["This is a test for balancing"],
          improvements: ["This is a test for improvements"],
          fixes: ["This is a test for bugfixes", "bugfix2"],
        }),
        new Version("MINOR", "test", {
          features: ["Versioning"],
          balancing: ["This is a test for balancing"],
          improvements: ["This is a test for improvements"],
          fixes: ["This is a test for bugfixes", "bugfix3"],
        }),
      ],
    };
  },
  mutations: {},
  actions: {},
  getters: {
    /**
     *
     * @param state - The state of module
     * @returns {{version, number}[]} An Array containing a fitting list of version numbers according to state.versions
     */
    getVersions(state) {
      let versionNumbers = [];
      let currentMajor = 0;
      let currentMinor = 0;
      let currentPatch = 0;

      state.versions
        .slice()
        .reverse()
        .forEach((version) => {
          switch (version.type) {
            case "MAJOR":
              currentMajor += 1;
              currentMinor = 0;
              currentPatch = 0;
              break;
            case "MINOR":
              currentMinor += 1;
              currentPatch = 0;
              break;
            case "PATCH":
              currentPatch += 0;
              break;
            default:
              break;
          }

          versionNumbers.unshift({
            data: version,
            number: `${currentMajor}.${currentMinor}.${currentPatch}`,
          });
        });

      return versionNumbers;
    },
  },
};

export default versioningModule;
