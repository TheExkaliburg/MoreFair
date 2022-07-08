<template>
  <div v-if="store.getters.isMod" class="m-1">
    <div class="container-fluid">
      <div class="row">
        <div class="col">
          <div class="row m-1">
            <div class="input-group">
              <span class="input-group-text"> Find Account-ID: </span>
              <input
                v-model="searchUsernameInput"
                class="form-control shadow-none"
                maxlength="64"
                name="name"
                placeholder="Username"
                type="text"
                @keydown.enter="searchUserName"
              />
              <button
                class="btn btn-outline-primary shadow-none"
                href="#"
                type="submit"
                @click="searchUserName"
              >
                Send
              </button>
            </div>
          </div>
          <div class="row m-2">
            <div>Result: {{ moderation.userNameSearchResults }}</div>
          </div>
          <div class="row m-1">
            <div class="input-group">
              <span class="input-group-text"> Take Action: </span>
              <input
                v-model="actionUserIdInput"
                class="form-control shadow-none"
                maxlength="64"
                placeholder="Account-ID"
                type="text"
              />
              <button
                class="btn btn-outline-primary shadow-none"
                href="#"
                @click="ban"
              >
                Ban
              </button>
              <button
                class="btn btn-outline-primary shadow-none"
                href="#"
                @click="mute"
              >
                Mute
              </button>
              <button
                class="btn btn-outline-primary shadow-none"
                href="#"
                @click="rename"
              >
                Rename
              </button>
              <button
                class="btn btn-outline-primary shadow-none"
                href="#"
                @click="free"
              >
                Free
              </button>
              <button
                v-if="store.state.user.accessRole === 'OWNER'"
                class="btn btn-outline-primary shadow-none"
                href="#"
                @click="mod"
              >
                Mod
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useStore } from "vuex";
import { computed, inject, ref } from "vue";
import API from "@/websocket/wsApi";
// sdad
const store = useStore();
const stompClient = inject("$stompClient");

const moderation = computed(() => store.state.mod);

const searchUsernameInput = ref("");
const actionUserIdInput = ref("");

function searchUserName() {
  store.dispatch({ type: "mod/searchName", name: searchUsernameInput.value });
}

function ban() {
  if (confirm(`Are you sure you want to ban (#${actionUserIdInput.value})`)) {
    stompClient.send(
      API.MODERATION.APP_BAN_DESTINATION(actionUserIdInput.value)
    );
  }
}

function mute() {
  if (confirm(`Are you sure you want to mute (#${actionUserIdInput.value})`)) {
    stompClient.send(
      API.MODERATION.APP_MUTE_DESTINATION(actionUserIdInput.value)
    );
  }
}

function rename() {
  const newName = prompt(
    `What would you like to name (#${actionUserIdInput.value})`
  );
  if (newName) {
    stompClient.send(
      API.MODERATION.APP_RENAME_DESTINATION(actionUserIdInput.value),
      {
        content: newName,
      }
    );
  }
}

function free() {
  if (confirm(`Are you sure you want to free (#${actionUserIdInput.value})`)) {
    stompClient.send(
      API.MODERATION.APP_FREE_DESTINATION(actionUserIdInput.value)
    );
  }
}

function mod() {
  if (confirm(`Are you sure you want to mod (#${actionUserIdInput.value})`)) {
    stompClient.send(
      API.MODERATION.APP_MOD_DESTINATION(actionUserIdInput.value)
    );
  }
}
</script>

<style lang="scss" scoped></style>
