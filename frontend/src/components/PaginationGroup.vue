<template>
  <div class="col dropdown-pagination">
    <div class="btn-group dropdown">
      <button
        :class="max === 1 ? 'disabled' : ''"
        class="btn btn-outline-primary shadow-none"
        data-number="1"
        @click="onChange"
      >
        &laquo;
      </button>
      <button
        :class="current === 1 ? 'active' : ''"
        :data-number="current === 1 ? current : current - 1"
        class="btn btn-outline-primary shadow-none"
        @click="onChange"
      >
        {{ current === 1 ? current : current - 1 }}
      </button>
      <button
        :class="[max === 1 ? 'disabled' : '', current === 1 ? '' : 'active']"
        :data-number="current === 1 ? current + 1 : current"
        class="btn btn-outline-primary shadow-none"
        @click="onChange"
      >
        {{ current === 1 ? current + 1 : current }}
      </button>
      <button
        :class="max >= Math.max(current, 2) + 1 ? '' : 'disabled'"
        :data-number="current === 1 ? current + 2 : current + 1"
        class="btn btn-outline-primary shadow-none"
        @click="onChange"
      >
        {{ current === 1 ? current + 2 : current + 1 }}
      </button>
      <button
        :class="max === 1 ? 'disabled' : ''"
        :data-number="max"
        class="btn btn-outline-primary shadow-none"
        @click="onChange"
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
            :data-number="i"
            class="dropdown-item"
            href="#"
            @click="onChange"
          >{{ i }}</a
          >
        </li>
        <li v-if="showLast && last > max">
          <a
            :data-number="last"
            class="dropdown-item"
            href="#"
            @click="onChange"
          >{{ last }}</a
          >
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from "vue";

defineProps({
  max: { type: Number, required: true, default: 1 },
  current: { type: Number, required: true, default: 1 },
  // TODO: make this dynamic; currently size does nothing
  size: { type: Number, default: 3 },
  onChange: Function,
  last: { type: Number, required: false, default: 1 },
  showLast: { type: Boolean, default: false }
});
</script>

<style scoped>
ul {
  max-height: calc(16px + 32px * 5);
  overflow-y: auto;
}
</style>
