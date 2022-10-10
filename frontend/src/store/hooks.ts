import { defineStore } from "pinia";

export const useHooksStore = defineStore("hooks", () => {
  const onTicks = ref<Function[]>([]);

  function subscribeToHook(id: string, callback: Function) {
    switch (id) {
      case "onTick":
        onTicks.value.push(callback);
        break;
    }
  }

  return {
    onTicks,
    subscribeToHook,
  };
});
