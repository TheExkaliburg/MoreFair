<template>
  <div>
    <button class="w-full" @click="open = true">
      <slot name="button" />
    </button>
    <Dialog :open="open" class="relative z-50" @close="open = false">
      <div aria-hidden="true" class="fixed inset-0 bg-black/50" />
      <div class="fixed inset-0 flex items-center justify-center p-4">
        <DialogPanel
          class="w-full max-w-sm rounded-3xl bg-modal-bg text-modal-text p-4"
        >
          <DialogTitle class="text-2xl text-modal-text-title"
            ><span v-if="props.title.length > 0">{{ props.title }}</span>
            <slot name="title"
          /></DialogTitle>
          <DialogDescription class="text-sm"
            ><span v-if="props.description.length > 0">{{
              props.description
            }}</span>
            <slot name="description"
          /></DialogDescription>
          <slot />
        </DialogPanel>
      </div>
    </Dialog>
  </div>
</template>

<script lang="ts" setup>
import {
  Dialog,
  DialogDescription,
  DialogPanel,
  DialogTitle,
} from "@headlessui/vue";

const open = ref<boolean>(false);

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const props = defineProps({
  title: {
    type: String,
    required: false,
    default: "",
  },
  description: {
    type: String,
    required: false,
    default: "",
  },
});
</script>

<style scoped lang="scss"></style>
