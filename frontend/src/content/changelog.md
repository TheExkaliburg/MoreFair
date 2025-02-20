---
description: 'A list of all changes made to the game'
---

# Changelog

## 3.0.6 [PATCH]: Untitled

<br><br><br><br><br><br><br>

<details>
<summary>Expand for the 3.0.x changelog</summary>

## 3.0.5 [PATCH]: L1 Lave Hotfix

### Bugfixes
- LAVA cannot roll on L1 anymore.

### Improvements
- Adding Tuna to the Mod-List in Rules.

## 3.0.4 [PATCH]: Lava Hotfix

### Improvement:
- Disabling LAVA to its previous form (0 FloorGrapes). I would like to bring it back as a negative modifier later, but first I need time to implement a solution.
- Also, it can still roll but behaves as DROUGHT before.

## 3.0.3 [PATCH]: Vinegar Round Modifier Adjustments

### Features
- Can now roll positive and negative Vinegar Ladder-Mods onto the same ladder.

### Improvements
- CHAOS allows all Vinegar Ladder-Mods
- First Person out of AH, gets the 2AH-Points for throwing on AH-Ladder for free.

### Balancing
- CONSOLATION and GENEROUS rewards decreased and brought into line with BOUNTIFUL.
- STINGY/DROUGHT/NO_HANDOUTS modifiers buffed and renamed to TAX/LAVA/VIRUS to better reflect their changed behaviour.

### Bugfixes
- Last Person to promote from AH, now gets their promotedOn flag set correctly.
- SLOW not appropriately affecting all kinda modifiers (f.e. NO_AUTO)

## 3.0.2 [PATCH]: Reward Changes

### Improvements
- Scaling the Rewards for Winning slightly different.

### Bugfixes
- You now don't get your rewards downgraded by 1 place when winning.

## 3.0.1 [PATCH]: First small fixes

### Improvements
- Skipping Ticks after big catchup tick
- Quick-Button for Vinegar-Throw-Percentages now are 25/50/100.

### Bugfixes
- Eta To VinegarThrow now takes split into account
- Always showing Vinegar on the Button.
- Winning rewards are based on current ladder instead of next one
- Removed console.log()
- Fixing tooltips that still show 50% as minimum percentage.

## 3.0.0 [MAJOR]: Season 3 Overhaul

There are a few new features and a lot more small changes/improvements.
I'll deliberately stay vague, so the community can figure details out.

### Features:

- A new season approaches! Asshole points will be reset and the round numbers will start again from 1.
- Players can now choose to devote a portion of their vinegar generation to create wine at a higher rate instead.
- Players can now choose a percentage of their vinegar to throw, from 50% to 100%, in 1% increments.
- There is now a log for vinegar throws by/against you.
- There are a bunch of different ladder/round modifiers that you can encounter.
- The way asshole points are awarded has been changed.
  - There is currently a total of 13 AH-Points to earn per round.
- The progression of the symbols, awarded based on asshole points, have been changed.

### Improvements

- FAST/SLOW doesn't increase the amount of ladders in a round anymore. *This is a buff*.
- The tutorial has been updated to reflect the modern game and explain the new vinegar mechanics.
- The amount of players able to become assholes has been changed.
- Asshole points will no longer have any effect on the number of ladders in a round.
- Various other improvements and bugfixes, as well as new bugs for you to find.


</details>

<details>
<summary>Expand for the 2.5.x changelog</summary>

## 2.5.1 [PATCH]: Some smaller community-improvements

### Improvements:
- L1 can now roll all the modifiers besides NO_AUTO, FREE_AUTO and TINY
- Improving the timestamp on messages for some localizations

### Bugfixes:
- Manually sending empty metadata won't cause an error for clients anymore

## 2.5.0 [MINOR]: Ignore-List and END ladder modifier

### Features:

- An ignore list has been added to the Settings menu, above the theme selector.
  - Adding a player ID (the subscript number following their name) to the list suppresses all messages from that player
    in GLOBAL and LADDER chats.
  - A page reload is required to clear existing messages from ignored players.
  - Messages in the SYSTEM and MOD chats cannot be ignored.
- Adding the END Ladder Type after the AH Ladder as preparation for an updated S3 Logic.
- Adding Logic and Modifier for Round 200
- Adding an internal modifier for ladder scaling and adding the REVSC round modifier, that can't appear in the game, besides for round 200

### Improvements:

- The combined mod chat view now shows the correct ladder number for messages originating from LADDER chats.
- When filtering by Name, it doesn't show all the Mystery Guests in the suggestion anymore.
- Changing some internal logic regarding the resets of the round.

### Bugfixes:

- Renaming now also changes the name displayed in the suggestions
- - Clearing Messages of Banned/Muted Players from Chat

</details>

<details>
<summary>Expand for the 2.4.x changelog</summary>

## 2.4.0 [MINOR]: Global Chat and Channels

### Features:

- The Chat is broken down in Channels now
- These Channels are currently GLOBAL, LADDER, SYSTEM, MOD
  - GLOBAL is a single default chat for everyone, since some ladder-chats are rather dead
  - LADDER is what you know from before, 1 Chat for each Ladder, but you can only participate in the Ladder you are
    currently climbing
  - SYSTEM messages are announcements, like the Messages from Chad
  - MOD is a channel where mods can highlight their messages for moderation purposes
- you can toggle the LADDER Chat regarding whether you want to see local messages

### Improvements:

- Suggestions are now based of L1 accounts
- Messages that are not from global chat get highlighted
- Vinegar eta is back thanks to Raldec

</details>

<details>
<summary>Expand for the 2.3.x changelog</summary>

## 2.3.3 [PATCH]: Scroll Improvements and Iframes

### Features:

- You can now choose a 4th parameters to filter the rankers by
    4. By their position relative to the bottom
- Adding another option to hide the zombies (+0x1 rankers)

### Improvements:

- follow Ranker should now try to center your Ranker
- the game should now be iframe-able to allow for galaxy.click to embed it
- FairWiki now opens in a new tab
- adding the new Mods to the rules

### Hotfixes:

- Fixing the submit button in the signup form to not work

## 2.3.2 [PATCH]: Show all Ranker option

### Features:

- Can now disable the option to show all rankers in the ladder
- You can then choose 3 parameters to filter the rankers by
    1. By their position relative to the top
    2. By their position relative to you (above)
    3. By their position relative to you (below)

### Bugfixes:

- UI now properly updates the grapes when you are alone on a ladder
- Play sound on reaching first, if the option is set
- Fixing the chat that moved out of the screen in a long message
- Also mentioning something in a long message should not linewrap anymore

## 2.3.1 [PATCH]: Bugfixes

### Bugfixes:

- Fixing that Enter after autocomplete doesn't send the message
- Changelog Symbol in the sidebar actually sends you to the changelog instead of the rules
- Group Mentions actually are playing a notification sound now
- a joining player will now show the correct asshole-points
- if you are less than 5 minutes away from the next bias/multi, the color in the table will be yellow instead of red

## 2.3.0 [MINOR]: Frontend Redesign

### Frontend Update

- Adding a login page and the ability to link your uuid based guest-account to an email address
- Making the design mobile-friendly (responsive) and overhauling the entire design of the page
- Adding a separate Wiki that can be used by everyone to compile information
- Adding a small tutorial for when you first start the game
- Adding a lock-buttons button to the top right corner of the screen
- Adding toast-notifications, to give you additional feedback about actions
- Adding tooltips to some buttons
- Changed the eta formulas
- Restructuring the code to make it more maintainable
- Adding an impressum and a privacy policy

</details>

<details>
<summary>Expand for the 2.2.x changelog</summary>

## 2.2.7 [PATCH]: Changing the penalty for getting graped

Balancing:
Normally you would either get forced to multi or loose half of your power and get set back to 0 points. Now you will
loose 1 of your multi. So if you have 4 multi, getting graped sets you back to x3 +0 as if you just multied from x2.

### Rule-Changes:

- Adding 2 new rules, see discord for the reasoning for these rules.
- Rule 10: Griefing is prohibited; repeatedly and intentionally making the game worse for everybody else can lead to a
  warning and ban.
- Rule 11: All members of the community are expected to comply with official requests from moderators. If you think any
  mod is misusing their power please write @kaliburg a message on discord.

### Balancing:

- Normally you would either get forced to multi or loose half of your power and get set back to 0 points.
- Now you will loose 1 of your multi. So if you have 4 multi, getting graped sets you back to x3 +0 as if you just
  multied from x2.

## 2.2.6 [PATCH]: Hotfix for L1 not always rolling DEFAULT

### Fixes:

- L1 is now always rolling DEFAULT, even if it's a CHAOS round

## 2.2.5 [PATCH]: FREE_AUTO formula change

### Balancing:

- SLOW and FAST no longer impact how close the FREE_AUTO gets applied to ladders
- FREE_AUTO now applies to the ladder = floor(topLadder / 2) - 2
- This might not apply instantly, but only once the next ranker promotes into a corresponding ladder

## 2.2.4 [PATCH]: Round Modifier impacts the Round Base Point Requirement

### Improvements:

- SLOW, FAST and CHAOS now slightly impact the Round Base Point Requirement.

## 2.2.3 [PATCH]: Back-to-Back Protection for Rounds

### Improvements:

adding a back-to-back protection for round-types, making it almost impossible to roll the same combinations of modifiers
for the round twice

## 2.2.2 [PATCH]: Spark and Statistics Endpoints

### Features:

- using spark and mongodb to get some more accurate statistics regarding the game

### API-Changes:

- changing the api for the raw round stats from /roundStats /api/stats/round/raw
- adding a new api endpoint to receive the equivalent of the community-created 'Champions of the Ladder' at
  /api/stats/round
- adding a new api endpoint for a analysis of the activity in the last 28 days at /api/stats/activity

## 2.2.1 [PATCH]: CHEAP and EXPENSIVE also scales with ladders

### Balancing:

- CHEAP ladders have the cost to bias and multi reduced as if they are half their ladder number and then by and
  additional 50%.
- EXPENSIVE ladders have the cost to bias and multi increased as if they are 1.5x their ladder number and then by and
  additional 50%.

## 2.2.0 [MINOR]: CHEAP and EXPENSIVE Ladder Types

### Features:

- CHEAP ladders have the cost to bias and multi reduced by 50%.
- EXPENSIVE ladders have the cost to bias and multi increased by 50%.
- CHEAP ladders are more common on FAST rounds, and EXPENSIVE ladders are more common on SLOW rounds.
- CHAOS rounds have a much higher chance of rolling CHEAP or EXPENSIVE ladders, with equal chance for each.

</details>