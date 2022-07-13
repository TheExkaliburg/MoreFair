import Version from "@/versioning/entities/version";

const versioningModule = {
  namespaced: true,
  state: () => {
    return {
      versions: [
        new Version("MINOR", "Round-Rework", {
          rules: ["Rule 8 now only allows for alts up to Ladder 5"],
          features: [
            "Rounds and ladders can have specific modifier (some can even have multiple at once).",
            "DEFAULT Round and Ladders are the same as before.",
            "SMALL Ladders only have around a tenth of the size of a DEFAULT one",
            "BIG Ladders have around 3 times the size of a DEFAULT one",
            "NO_AUTO Ladders replace mostly the logic of the asshole ladder, because you can't buy auto promote there. Nevertheless, theres a small chance to have another ladder be a NO_AUTO ladder. This would overwrite the free autos from the AUTO rounds for this round.",
            "ASSHOLE Ladder, I Believe you know this already, but the last ladder now has an exclusive type to make the identification of that ladder easier",
            "FAST Rounds only have SMALL Ladders and the asshole ladder is at half the original value",
            "AUTO Rounds give people auto-promote automatically on entering a Ladder, for free (Does not work for Asshole-Ladder)",
          ],
          balancing: [
            "The base points requirement of a round and a ladder are randomized now (before the new ladder and round types).",
            "The base points requirement for a round is between 50K - 150K",
            "The base points requirement for a ladder is based on the round requirement multiplied by the ladder number and a value between 1.2 - 0.8",
            "The number of assholes that can pass the final ladder are now also randomized.",
            "The minimum amount of people to become an asshole is 5 (base asshole ladder / 2)",
            "The maximum amount of people to become an asshole is the ladder number of the asshole Ladder",
            "The asshole ladder has been capped at ladder 25 and won't grow unless there is a reason to go above that",
            "The rewards for coming first (or up to tenth) have been overworked.",
            "If you are first you still gain autoPromote and your current vinegar gets multiplied by 1.2",
            "If you are in the Top 3 (1st, 2nd, 3rd), you the amount of grapes you would need to buy a full-cost autopromote (5000 Grapes)",
            "If you are in the 4th or 5th position you get half the amount of a full-cost autopromote (2500 Grapes)",
            "If you are in the remaining 5 (5th to 10th) of the base amount of people that is needed to unlock the ladder, you gain a tenth of the grapes needed to buy a full-cost autopromote (500 Grapes)",
            "If you can't afford a multi on getting graped, you don't only loose all your points, but also half of your power",
          ],
          improvements: [
            "Throwing vinegar and buying auto-promote doesn't get send over public channel anymore",
            "Only active rankers count toward the asshole ladder",
            "Group mentions will now be suggested in the chat if you are subscribed to the group!",
            "Overhauled the Messages that Chad gives you for the asshole-promotions",
            "You get notified again, if you got graped down.",
            "Chat Messages can be 280 characters long again",
          ],
          fixes: [
            "Removed an option that didn't have any functionality",
            "Discord links now open in a new tab.",
          ],
        }),
        new Version("PATCH", "Group Mentions", {
          features: [
            "Group mentions are now supported in the chat. $Train$ Choo! Choo!",
          ],
          improvements: [
            "You can now click on a username in the chat to mention them.",
            "Moderators can now right click on the ladder to moderate people.",
          ],
          fixes: ["Fixed mention sound not playing."],
        }),
        new Version("PATCH", "Fixing Asshole-Badge Limit", {
          features: [
            "An old known sound now chimes whenever you promoted.",
            "Another sound now rings when you reached the top of a ladder",
          ],
          improvements: [
            "ETA to Top is now showing the eta until you could promote instead of the eta to the first place",
          ],
          fixes: [
            "Trying to fix that you can scroll the username in the chat-messages.",
            "Fixing the eta under The auto-promote to always show infinity.",
            "Shown Symbol is capped at the highest available Symbol",
            "Adding 15 new Symbols (may change at a later date)",
          ],
        }),
        new Version("PATCH", "Floor Grapes on Top Ladder", {
          balancing: [
            "Removing the restriction from floor-grapes for the top ladder.",
          ],
          improvements: ["The Account id is now shown as subscript."],
          fixes: [
            "Message length indicator will have the right length while writing now",
          ],
        }),
        new Version("PATCH", "Theme Loader, ETA, Streamer QOL", {
          features: [
            "Added a theme loader to the Options menu.",
            "Load themes from URLs or load previously saved themes.",
            "You can also delete themes you don't like.",
            "You can now hide you vinegar and grape count.",
            "There will now be an ETA until your vinegar is enough to be thrown.",
            "You can now hide your Chat.",
            "You can now display some eta information in a color in the ladder.",
            "You can now display the eta for everyone to the top and to you",
            "You are now able to hide promoted players from the game.",
          ],
          improvements: [
            "Clicking on a navbar item that changes the view will now close the navbar.",
            "Better ETA to promotion.",
            "Better announcement Message for promotion",
            "Now also shows current ladder above the ladder-table",
            "Showing account-id in the ladder",
          ],
          fixes: [
            "Added a new color variable. This is used in the mention popup for theming.",
            "Fixing typo in the Help-Section",
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
