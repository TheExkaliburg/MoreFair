<template>
  <Dialog class="relative z-50" @close="close">
    <div aria-hidden="true" class="fixed inset-0 bg-black/50" />
    <div class="fixed inset-0 flex items-center justify-center p-4">
      <DialogPanel
        class="w-full max-w-sm rounded-3xl bg-modal-bg text-modal-text p-4 relative text-sm overflow-y-auto"
      >
        <button
          class="absolute top-2 right-4 text-text hover:text-text-light"
          @click="$emit('close')"
        >
          x
        </button>
        <DialogTitle class="text-2xl text-modal-text-title"
          ><span v-if="props.title.length > 0">{{ props.title }}</span>
          <slot name="title" />
        </DialogTitle>
        <DialogDescription
          ><span v-if="props.description.length > 0">{{
            props.description
          }}</span>
          <slot name="description" />
        </DialogDescription>
        <slot />
      </DialogPanel>
    </div>
  </Dialog>
</template>

<script lang="ts" setup>
import {
  Dialog,
  DialogDescription,
  DialogPanel,
  DialogTitle,
} from "@headlessui/vue";

const emit = defineEmits(["close"]);

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
  clickOutsideToClose: {
    type: Boolean,
    required: false,
    default: true,
  },
});

function close() {
  if (props.clickOutsideToClose) {
    emit("close");
  }
}
</script>
