<template>
  <nav class="navbar px-2">
    <router-link class="navbar-brand" to="/">
      <img
        class="d-inline-block align-top"
        height="30"
        src="/favicon.ico"
        width="30"
      />
      <span>More Fair Game</span>
    </router-link>
    <button
      aria-controls="navbarNav"
      aria-expanded="false"
      aria-label="Toggle navbar"
      class="navbar-toggler main-color"
      data-bs-target="#offcanvas"
      data-bs-toggle="offcanvas"
      type="button"
    >
      <i class="bi bi-list main-color" />
    </button>
  </nav>
  <div
    id="offcanvas"
    aria-labelledby="offcanvasLabel"
    class="offcanvas offcanvas-end"
    tabindex="-1"
  >
    <div class="container">
      <div class="row">
        <div class="col-8" data-bs-toggle="offcanvas">
          <router-link to="/">Game</router-link>
        </div>
        <div class="col-4">
          <button
            class="col-12 btn btn-sm btn-outline-primary shadow-none"
            @click="promptNameChange"
          >
            Change Name
          </button>
        </div>
      </div>
      <div class="row">
        <div class="col-8" data-bs-toggle="offcanvas">
          <router-link to="/options">Options</router-link>
        </div>
        <div class="col-4">
          <button
            class="col-12 btn btn-sm btn-outline-primary shadow-none"
            @click="exportCookie"
          >
            Export
          </button>
        </div>
      </div>
      <div class="row">
        <div class="col-8" data-bs-toggle="offcanvas">
          <router-link to="/help">Help</router-link>
        </div>
        <div class="col-4">
          <button
            class="col-12 btn btn-sm btn-outline-primary shadow-none"
            @click="importCookie"
          >
            Import
          </button>
        </div>
      </div>
      <div class="row">
        <div class="col-8" data-bs-toggle="offcanvas">
          <router-link to="/changelog">Changelog</router-link>
        </div>
      </div>
      <div
        v-if="
          store.getters['options/getOptionValue']('enableModPage') &&
          store.getters.isMod
        "
        class="row"
      >
        <div class="col-8" data-bs-toggle="offcanvas">
          <router-link to="/mod">Moderation</router-link>
        </div>
      </div>
      <div class="row">
        <div class="col-8">
          <a href="https://discord.gg/ThKzCknfFr" target="_blank">Discord</a>
        </div>
      </div>
    </div>
  </div>
  <div class="view">
    <router-view />
  </div>
</template>

<script setup>
import { useStore } from "vuex";
import Cookies from "js-cookie";
import API from "@/websocket/wsApi";
import { provide } from "vue";
import { StompClient } from "@/websocket/stompClient";

//import { hooksSystemSetup } from "@/modules/hooks";

//hooksSystemSetup();

const stompClient = new StompClient();
provide("$stompClient", stompClient);

const store = useStore();

//Prompt the modules/options to load the options
store.commit("options/init");
store.commit("options/loadOptions");

store.dispatch({ type: "setupConnection", stompClient: stompClient });

function promptNameChange() {
  let newUsername = window.prompt(
    "What shall be your new name? (max. 32 characters)",
    store.state.ladder.yourRanker.username
  );
  if (newUsername && newUsername.length > 32) {
    let temp = newUsername.substring(0, 32);
    alert(
      "The maximum number of characters in your username is 32, not " +
        newUsername.length +
        "!"
    );
    newUsername = temp;
  }

  if (
    newUsername &&
    newUsername.trim() !== "" &&
    newUsername !== store.state.ladder.yourRanker.username
  ) {
    stompClient.send(API.ACCOUNT.APP_RENAME_DESTINATION, {
      content: newUsername,
    });
  }
}

async function importCookie() {
  let newUUID = prompt(
    "Paste your ID into here (your old uuid will be copied into your clipboard):"
  );
  try {
    if (newUUID) {
      // TODO: Check if cookies are valid
      let oldUuid = Cookies.get("_uuid");
      Cookies.set("_uuid", newUUID, {
        expires: 10 * 365,
        secure: true,
        sameSite: "Lax",
      });
      await setTimeout(
        async () => await navigator.clipboard.writeText(oldUuid),
        1000
      );
      // Relaod the page for the new cookies to take place
      setTimeout(() => location.reload(), 1500);
    }
  } catch (err) {
    alert("Invalid ID!");
    console.error(err);
  }
}

async function exportCookie() {
  // Copy the text inside the text field
  await navigator.clipboard.writeText(Cookies.get("_uuid"));

  // Alert the copied text
  alert("Copied your ID to your clipboard! (don't lose it or give it away!)");
}
</script>

<style lang="scss" scoped>
@import "./styles/styles";

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
}

.navbar-brand:hover {
  color: var(--link-color);
}

.row {
  padding-top: 15px;
  padding-bottom: 15px;

  .col-8 {
    text-align: start;
    padding-left: 15%;

    a {
      text-decoration: none !important;
    }

    a:not(:hover) {
      color: var(--main-color) !important;
    }
  }
}

nav {
  padding: 8px;

  a {
    font-weight: bold;
  }
}

.view {
  max-height: calc(100vh - 56px);
}
</style>
