import Version from "@/versioning/entities/version";

const versioningModule = {
  namespaced: true,
  state: () => {
    return {
      versions: [
        new Version("PATCH", "Theme Loader", {
          features: [
            "Added a theme loader to the Options menu.",
            "Load themes from URLs or load previously saved themes.",
            "You can also delete themes you don't like.",
            "You can now hide you vinegar and grape count.",
          ],
        }),
        new Version("PATCH", "Changelog", {
          features: [
            "Adding a Changelog and a versioning System on the Main Page",
            "Adding a Message-Length-Indicator",
          ],
          improvements: [
            "Mention-Sounds should now only be played if you have the chat open the moment you receive them",
          ],
        }),
        new Version("MINOR", "Theme Selector", {
          features: [
            "Adding Theme Selector in Options ((creation of themes in the source code is easy, so we can also host some nice looking ones, that are community made)",
            "Adding the Light Theme (its a work in progress but i dont want players to get headaches 😉)",
          ],
          improvements: [
            "Changing some colors for promoted players in the default theme",
            "Mentions are better sorted if you type @#id",
            "Mention-Sounds should only be played once now (apparently they are only fixed when swapping between views not when changing chats)",
          ],
          fixes: [
            "Fixing the double sorting of the ladder after each update (should increase the performance, hopefully should not reintroduce the graping bug)",
            "Changing your view to a higher ladder as a moderator should not break the game anymore",
          ],
        }),
        new Version("PATCH", "Bugfixes", {
          fixes: [
            "Quick hotfix for the chat bug where you enter a space at the end of the input line",
          ],
        }),
        new Version("PATCH", "/lastRound Stats", {
          features: [
            "Fast update to allow for the snapshot of the game at the moment it ended from last round to be accessed via /lastRound (this doesn't work until a round is finished and resets on restart but its better than what clu is currently using)",
          ],
        }),
        new Version("PATCH", "Bugfixes", {
          improvements: ["you can see active rankers at the top again"],
          fixes: [
            "dropdown from the mentioning system shouldn't stay sometimes anymore",
            "changing the way the options are being turned off/on (should not prompt you mod-options now)",
            "the graping desync where the player would keep their rank after being graped is gone",
            "you cannot paste everything into the console anymore (even though it didn't actually send it before)",
            "chat input box should not grow anymore if you type too much (and grow out of the screen)",
            "reloading the /options or /help page should now take you back there instead of giving you an error screen",
          ],
        }),
        new Version("MAJOR", "New Frontend and Design", {
          features: ["A new frontend is here"],
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
              currentMajor++;
              currentMinor = 0;
              currentPatch = 0;
              break;
            case "MINOR":
              currentMinor++;
              currentPatch = 0;
              break;
            case "PATCH":
              currentPatch++;
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
