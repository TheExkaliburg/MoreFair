<template>
  <div class="col chat-pagination">
    <div class="btn-group dropdown">
      <button
        :class="max === 1 ? 'disabled' : ''"
        class="btn btn-outline-primary shadow-none"
        data-chat="1"
        @click="onChange()"
      >
        &laquo;
      </button>
      <button
        :class="current === 1 ? 'active' : ''"
        :data-chat="current === 1 ? current : current - 1"
        class="btn btn-outline-primary shadow-none"
        @click="changeChat"
      >
        {{ current === 1 ? current : current - 1 }}
      </button>
      <button
        :class="[max === 1 ? 'disabled' : '', current === 1 ? '' : 'active']"
        :data-chat="current === 1 ? current + 1 : current"
        class="btn btn-outline-primary shadow-none"
        @click="changeChat"
      >
        {{ current === 1 ? current + 1 : current }}
      </button>
      <button
        :class="max >= Math.max(current, 2) + 1 ? '' : 'disabled'"
        :data-chat="current === 1 ? current + 2 : current + 1"
        class="btn btn-outline-primary shadow-none"
        @click="changeChat"
      >
        {{ current === 1 ? current + 2 : current + 1 }}
      </button>
      <button
        :class="max === 1 ? 'disabled' : ''"
        :data-chat="max"
        class="btn btn-outline-primary shadow-none"
        @click="onChange()"
      >
        &raquo;
      </button>
      <button
        class="btn btn-outline-primary shadow-none dropdown-toggle"
        data-bs-toggle="dropdown"
      ></button>
      <ul class="dropdown-menu">
        <li v-for="i in max" :key="i">
          <a
            :data-chat="i"
            class="dropdown-item"
            href="#"
            @click="onChange()"
            >{{ i }}</a
          >
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from "vue";

const props = defineProps({
  max: { type: Number, required: true },
  current: { type: Number, required: true },
  size: { type: Number, default: 3 },
  onChange: Function,
});

console.log(props);
</script>

<style scoped></style>
