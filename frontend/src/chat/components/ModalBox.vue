<template>
  <div>
    <div id="openTrigger" @click="openModal">
      <slot name="trigger">...</slot>
    </div>
    <div v-show="visible">
      <div class="backdrop" @click="closeModal"></div>
      <div class="modal">
        <div class="title">
          <slot name="title"></slot>
        </div>
        <div class="content">
          <slot></slot>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";

// variables
const visible = ref(false);

function closeModal() {
  visible.value = false;
}
function openModal() {
  visible.value = true;
}
</script>

<style lang="scss" scoped>
@import "../../styles/styles";

.backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 9999998; //always on top!
}

.title {
  font-size: 2em;
  color: black;
  white-space: nowrap;
  text-align: center;
  text-decoration: underline;
  width: 100%;
}

.content {
  padding: 1em;
  width: 100%;
  height: fit-content;
  overflow: auto;
  color: black;
  text-align: center;
  white-space: pre-wrap;
}

.modal {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80%;
  max-width: 500px;

  height: 80%;
  max-height: 500px;

  display: block;

  background-color: rgb(200, 200, 200);
  border-radius: 15px;
  border: 1px solid var(--main-color);
  padding: 20px;
  z-index: 9999999; //always on top!
}
</style>
