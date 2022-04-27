<template>
  <div v-if="store.getters.isMod" class="m-1">
    <div class="container-fluid">
      <div class="row">
        <div class="col-6">
          <div class="row m-1">
            <div class="input-group">
              <span class="input-group-text"> Find Account-ID: </span>
              <input
                id="usernameLookup"
                class="form-control shadow-none"
                maxlength="64"
                placeholder="Username"
                type="text"
              />
              <button
                id="lookupButton"
                class="btn btn-primary shadow-none"
                href="#"
              >
                Send
              </button>
            </div>
          </div>
          <div class="row m-2">
            <div id="lookupResult">Result: [0]</div>
          </div>
          <div class="row m-1">
            <div class="input-group">
              <span class="input-group-text"> Force Event: </span>
              <input
                id="idInput"
                class="form-control shadow-none"
                maxlength="64"
                placeholder="Account-ID"
                type="text"
              />
              <button
                id="idButton"
                class="btn btn-primary shadow-none"
                href="#"
              >
                Send
              </button>
            </div>
          </div>
          <table class="table table-sm table-striped table-borderless">
            <thead>
              <tr>
                <th class="col-4"></th>
                <th class="col-2"></th>
                <th class="col"></th>
              </tr>
            </thead>
            <tbody id="updateBody"></tbody>
          </table>
        </div>
        <div class="col-6">
          <ChatWindow />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useStore } from "vuex";
import ChatWindow from "@/chat/components/ChatWindow";

const store = useStore();
</script>

<style lang="scss" scoped></style>
