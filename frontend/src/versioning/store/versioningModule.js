import Version from "@/versioning/entities/version";

const versioningModule = {
  namespaced: true,
  state: () => {
    return {
      versions: [
        new Version("PATCH", "Alt-Checks", {
          features: ["Moderators should now be able to check for alts."],
        }),
        new Version("PATCH", "Timestamp for messages", {
          fixes: [
            "Should now show the timestamp of the message correctly, regarding timezone and locale",
          ],
        }),
        new Version("PATCH", "Backslash breaking Chad - Fix v2", {
          fixes: [
            "Should escape the usernames properly on sending a message from Chad.",
          ],
        }),
        new Version("PATCH", "Backslash breaking Chad - Fix", {
          fixes: [
            "Should escape the usernames properly on sending a message from Chad.",
          ],
        }),
        new Version("PATCH", "Client Side Performance", {
          improvements: [
            "Excluded the insides of the Decimals from the reactive state since they never change, only get overwritten.",
            "This was back when i tested it (August) a ~50% decrease in time it takes for a tick to process",
          ],
        }),
        new Version("PATCH", "Deleting messages of muted/banned players", {
          fixes: [
            "Should now properly delete messages of muted/banned players",
          ],
        }),
        new Version("PATCH", "Balancing SLOW + CHAOS", {
          balancing: [
            "CHAOS also has a chance to contain DEFAULT Ladders",
            "SLOW doesn't contain SMALL and TINY anymore",
            "SLOW contains less DEFAULT Ladders",
          ],
          fixes: [
            "FAST + CHAOS can now generate something else besides SMALL ladders",
          ],
        }),
        new Version("PATCH", "Spectate Asshole Ladder", {
          features: [
            "Adding an option to being able to spectate the asshole ladder (experimental)",
          ],
        }),
        new Version("PATCH", "SLOW fixes", {
          balancing: [
            "Slightly boosting the chance for GIGANTIC ladders in SLOW rounds to an avg. of 1-2 per rounds",
          ],
          fixes: [
            "Fixed a bug where SLOW still has back to back protection from repeating BIG ladders",
          ],
        }),
        new Version("PATCH", "Round- and LadderType Bugfixes", {
          fixes: [
            "Fixed a bug where SLOW would have 2/3 of their ladders FREE_AUTO",
            "CHAOS now gets applied before SLOW/FAST and should create even more interesting behaviour",
          ],
        }),
        new Version("MINOR", "CHAOS and SLOW Round Modifier", {
          features: [
            "CHAOS rounds have an equal chance to be TINY, SMALL, BIG or GIGANTIC.",
            "CHAOS rounds have a random length between base Asshole-Ladder and 25, ignoring the scaling from AH points.",
            "SLOW rounds have an higher Chance to roll BIG and GIGANTIC but less chance for SMALL and TINY",
            "SLOW rounds have a flat +5 modifier to their length and their base Asshole Ladder.",
          ],
          fixes: [
            "The first ladder of a [FAST,AUTO] round should now be correctly marked as [SMALL].",
          ],
        }),
        new Version("PATCH", "Fixing Mention-Sound and Emoji-QoL", {
          fixes: [
            "Fixed a bug where the gotFirstJingle played on mentions",
            "Increasing the min length of emoji strings to 3. You can now send `:)` and `:^)` without it being interpreted as an emoji.",
          ],
        }),
        new Version("PATCH", "Emojis and Follow Ranker", {
          features: [
            "You can now enable following your own ranker in the options :^)",
            "You can now use emojis in your messages :eyes:",
          ],
          balancing: [
            "A Ladder only gains FREE_AUTO dependent on the base Asshole-Ladder. So in DEFAULT rounds it's now 10 behind and in FAST rounds it's 5 behind.",
            "You don't gain any asshole-points by leaving L1 anymore. The lost point is now distributed to you reaching asshole-ladder.",
          ],
          fixes: [
            "Fixed theme loading issues.",
            "Fixed some sound settings.",
            'Fixed a bug with the "got first jingle" sound.',
            "Made the chad input system more maintainable.",
            "Fixed 2 bugs with rendering / sending mentions. Group mentions and user mentions should now be more stable.",
            "BIG can not be generated after another BIG/GIGANTIC anymore.",
          ],
        }),
        new Version("PATCH", "Balancing Round-Types", {
          balancing: [
            "FAST  rounds are only half as likely to appear now. (40% -> 20%)",
            "AUTO rounds are only half as likely to appear now. (20% -> 10%)",
            "NO_AUTO ladders were tuned down from 5% to 2% (unless its an auto round, where its still 5%)",
          ],
          api: [
            "Changed /lastRound to /roundStats",
            "Added an option to specify which round you want to pull the stats from with /roundStats?round=#",
            "The /roundStats provides the time, when the round was created and closed (also the time it was created for each ladder)",
          ],
        }),
        new Version("PATCH", "QoL and Options-Save-Fix", {
          improvements: [
            "Added a [Don't show this again] button to the help text.",
            "Changed the throw vinegar button text to clarify that all vinegar is thrown.",
          ],
          balancing: [
            "Doesn't Replace No-Auto Ladders with Free-Auto anymore.",
          ],
          fixes: ["Now saving all options (hopefully) correctly."],
        }),
        new Version("PATCH", "Grape reward changes", {
          balancing: [
            "Floor grapes are down to 2 from 3",
            "The cost for autopromote is down to 2000 from 5000",
            "This turns down all grape-rewards for promoting early by a factor of 3/5",
          ],
          improvements: [
            "Showing if someone can bias/multi now",
            "A little bit something to put the chat underneath the ladder for mobile users",
            "Temporary Help page link on the front page (you can turn it off in options/ladder)",
          ],
          fixes: [
            "Hopefully fixed a bug which saves the ladder-types multiple times in the database",
          ],
        }),
        new Version("PATCH", "Option for power-gain vs multi/bias", {
          improvements: [
            "Added an option to choose whether to show power-gain and multi/bias",
            "Showing bias/multi above bias/multi - button now",
          ],
        }),
        new Version("PATCH", "Hotfix for Help", {
          fixes: [
            "It should not show DEFAULT in L1 if it isn't a DEFAULT Ladder",
            "HelpView text should be fixed",
          ],
        }),
        new Version("PATCH", "Mostly Performance", {
          improvements: [
            "Chad will announce temporarily that the server is restarting",
          ],
          balancing: [
            "BIG Ladders now can spawn with 20% again, but there can't be 2 BIG ladders back to back.",
            "Free Auto Ladders, 5 behind the top are back for testing now",
          ],
          fixes: [
            "The winner of the round should now actually get 20% of their vinegar as reward",
            "Rankers don't get pulled with the account, this should increase server-performance a lot",
            "Broadcaster Account (Chad) only gets pulled from database the first time and then cached",
            "The join event now includes the asshole Points of that person",
            "Various other small performance fixes",
          ],
          api: [
            "The API is now exposed via the Fair.register((api) => ...) endpoint",
            "Only exposing the api.state and api.getters endpoint to the user now",
            "Also exposing a function to subscribe to a hook",
            "The first hook you can subscribe too is 'onTick' and gets called after every tick",
            "The API is readonly, so that 2 scripts can share the same api without 1 messing with the api and breaking it for the other",
          ],
        }),
        new Version("PATCH", "FREE_AUTO Ladders", {
          features: [
            "There is a FREE_AUTO LADDER and it automatically gives you autopromote",
            "Every ladder, that is 5 ladders behind the top ladder is getting turned into a FREE_AUTO Ladder (This doesn't work currently, will come back later)",
          ],
          balancing: [
            "Big Ladders are only half as likely to appear than before, should make streaks of them less likely",
          ],
          improvements: [
            "/lastRound Results now include the types and the basePointRequirements",
            "Tiny Performance increases",
          ],
          fixes: ["Muting now works correctly"],
        }),
        new Version("MAJOR", "Round-Rework and Big Reset", {
          rules: ["Rule 8 now only allows for alts up to Ladder 5"],
          features: [
            "Rounds and ladders can have specific modifier (some can even have multiple at once).",
            "DEFAULT Round and Ladders are the same as before.",
            "SMALL Ladders only have around a tenth of the size of a DEFAULT one",
            "BIG Ladders have around 3 times the size of a DEFAULT one",
            "NO_AUTO Ladders replace mostly the logic of the asshole ladder, because you can't buy auto promote there. Nevertheless, theres a small chance to have another ladder be a NO_AUTO ladder. This would overwrite the free autos from the AUTO rounds for this round.",
            "ASSHOLE Ladder, I Believe you know this already, but the last ladder now has an exclusive type to make the identification of that ladder easier",
            "Legends have it that there are rare and elusive TINY and GIGANTIC Ladders out there. But no one has ever seen them.",
            "FAST Rounds only have SMALL Ladders and the asshole ladder is at half the original value",
            "AUTO Rounds give people auto-promote automatically on entering a Ladder, for free (Does not work for Asshole-Ladder)",
            "You now gain asshole points on leaving the first ladder (1), reaching the base asshole ladder (1) and reaching the asshole ladder (1).",
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
            "You asshole points are now not mapped 1:1 to your asshole badge. Each Symbol requires 10 more points to reach it.",
            "If you press the asshole button you now gain 7 asshole points",
          ],
          improvements: [
            "Throwing vinegar and buying auto-promote doesn't get send over public channel anymore",
            "Only active rankers count toward the asshole ladder",
            "Group mentions will now be suggested in the chat if you are subscribed to the group!",
            "Overhauled the Messages that Chad gives you for the asshole-promotions",
            "You get notified again, if you got graped down.",
            "Chat Messages can be 280 characters long again",
            "Number formatting now starts after 6 digits and not after 10",
            "Now you can enable an option to show the asshole points next to the asshole tags",
          ],
          fixes: [
            "Removed an option that didn't have any functionality",
            "Discord links now open in a new tab.",
            "Overflow of the ranker table cells has been hidden, too long usernames no longer break the table format.",
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
