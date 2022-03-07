<template>
  <nav class="navbar">
    <a class="navbar-brand" href="#">
      <img
        class="d-inline-block align-top"
        height="30"
        src="/favicon.ico"
        width="30"
      />
      More Fair Game
    </a>
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
        <div class="col-8">
          <router-link to="/">Game</router-link>
        </div>
        <div class="col-4">
          <button class="col-12 btn btn-sm btn-outline-primary">Export</button>
        </div>
      </div>
      <div class="row">
        <div class="col-8">
          <router-link to="/options">Options</router-link>
        </div>
        <div class="col-4">
          <button class="col-12 btn btn-sm btn-outline-primary">Import</button>
        </div>
      </div>
      <div class="row">
        <div class="col-8">
          <router-link to="/help">Help</router-link>
        </div>
      </div>
      <div class="row">
        <div class="col-8">
          <a href="https://discord.gg/ThKzCknfFr">Discord</a>
        </div>
      </div>
    </div>
  </div>
  <router-view />
</template>

<script setup>
import { provide } from "vue";
import { useStore } from "vuex";
import Cookies from "js-cookie";
import { StompClient } from "@/websocket/stompClient";

const store = useStore();
const stompClient = new StompClient();

let setupPromise = setupConnection();
provide("$setupPromise", setupPromise);
provide("$stompClient", stompClient);

function setupConnection() {
  return new Promise((resolve) => {
    stompClient.connect(() => {
      stompClient.subscribe("/user/queue/info", (message) => {
        store.commit({ type: "initSettings", message: message });

        stompClient.subscribe("/user/queue/account/login", (message) => {
          store.commit({ type: "initUser", message: message });
          setupData(resolve);
        });

        let uuid = Cookies.get("_uuid");
        if (uuid === "" && !confirm("Do you want to create a new account?")) {
          return;
        }
        stompClient.send("/app/account/login");
      });

      stompClient.send("/app/info");
    });
  });
}

function setupData(resolve) {
  let highestLadderReached = store.state.user.highestCurrentLadder;
  stompClient.subscribe("/topic/chat/" + highestLadderReached, (message) => {
    store.commit({ type: "chat/addMessage", message: message });
  });
  stompClient.subscribe("/user/queue/chat/", (message) => {
    store.commit({ type: "chat/init", message: message });
  });
  stompClient.send("/app/chat/init/" + highestLadderReached);
  stompClient.subscribe("/topic/ladder/" + highestLadderReached, () => {
    //store.commit({ type: "chat/updateChat", message: message });
    resolve();
  });
  stompClient.subscribe("/topic/global/", (message) => {
    store.commit({ type: "chat/updateChat", message: message });
  });
  stompClient.subscribe("/user/queue/ladder/", () => {});
  stompClient.send("/app/ladder/init/" + highestLadderReached);
}
</script>

<style lang="scss">
@import "./styles/styles";

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
}

.navbar-brand:hover {
  color: $link-color;
}

.row {
  padding-top: 15px;
  padding-bottom: 15px;
  font-size: 24px;

  .col-8 {
    text-align: start;
    padding-left: 15%;

    a {
      text-decoration: none !important;
    }

    a:not(:hover) {
      color: $main-color !important;
    }
  }
}

nav {
  padding: 30px;

  a {
    font-weight: bold;

    &.router-link-exact-active {
      color: #a4dddb;
    }
  }
}
</style>
