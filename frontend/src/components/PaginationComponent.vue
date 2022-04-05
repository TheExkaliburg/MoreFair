<template>
  <!--div class="col chat-pagination">
    <div class="btn-group dropdown">
      <button
        :class="user.highestCurrentLadder === 1 ? 'disabled' : ''"
        class="btn btn-outline-primary shadow-none"
        data-chat="1"
        @click="changeChat"
      >
        &laquo;
      </button>
      <button
        :class="chat.currentChatNumber === 1 ? 'active' : ''"
        :data-chat="
          chat.currentChatNumber === 1
            ? chat.currentChatNumber
            : chat.currentChatNumber - 1
        "
        class="btn btn-outline-primary shadow-none"
        @click="changeChat"
      >
        {{
          chat.currentChatNumber === 1
            ? chat.currentChatNumber
            : chat.currentChatNumber - 1
        }}
      </button>
      <button
        :class="[
          user.highestCurrentLadder === 1 ? 'disabled' : '',
          chat.currentChatNumber === 1 ? '' : 'active',
        ]"
        :data-chat="
          chat.currentChatNumber === 1
            ? chat.currentChatNumber + 1
            : chat.currentChatNumber
        "
        class="btn btn-outline-primary shadow-none"
        @click="changeChat"
      >
        {{
          chat.currentChatNumber === 1
            ? chat.currentChatNumber + 1
            : chat.currentChatNumber
        }}
      </button>
      <button
        :class="
          user.highestCurrentLadder >= Math.max(chat.currentChatNumber, 2) + 1
            ? ''
            : 'disabled'
        "
        :data-chat="
          chat.currentChatNumber === 1
            ? chat.currentChatNumber + 2
            : chat.currentChatNumber + 1
        "
        class="btn btn-outline-primary shadow-none"
        @click="changeChat"
      >
        {{
          chat.currentChatNumber === 1
            ? chat.currentChatNumber + 2
            : chat.currentChatNumber + 1
        }}
      </button>
      <button
        :class="user.highestCurrentLadder === 1 ? 'disabled' : ''"
        :data-chat="user.highestCurrentLadder"
        class="btn btn-outline-primary shadow-none"
        @click="changeChat"
      >
        &raquo;
      </button>
      <button
        class="btn btn-outline-primary shadow-none dropdown-toggle"
        data-bs-toggle="dropdown"
      ></button>
      <ul class="dropdown-menu">
        <li v-for="i in user.highestCurrentLadder" :key="i">
          <a
            :data-chat="i"
            class="dropdown-item"
            href="#"
            @click="changeChat"
            >{{ i }}</a
          >
        </li>
      </ul>
    </div>
  </div--->
</template>

<script setup>
import { defineProps } from "vue";

const props = defineProps({
  max: { type: Number, required: true },
  current: { type: Number, required: true },
  size: { type: Number, default: 3 },
  onChange: Function,
});
</script>

<style scoped></style>
