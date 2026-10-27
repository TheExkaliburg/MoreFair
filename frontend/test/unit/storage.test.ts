// @vitest-environment happy-dom
import { afterEach, describe, expect, test } from "vitest";
import { createPinia, defineStore, setActivePinia } from "pinia";
import { effectScope, nextTick } from "vue";
import { useLocalStorage } from "~/composables/useLocalStorage";
import {
  BooleanOption,
  EnumOption,
  RangeOption,
} from "~/store/entities/option";

const scopes: ReturnType<typeof effectScope>[] = [];

afterEach(() => {
  scopes.forEach((scope) => scope.stop());
  scopes.length = 0;
  localStorage.clear();
});

describe("Pinia and VueUse saved options", () => {
  test("restores saved values, merges new defaults and retains option methods", async () => {
    localStorage.setItem(
      "migration-options",
      JSON.stringify({
        showEta: { value: false },
        theme: { value: "Light" },
        obsolete: true,
      }),
    );
    const scope = effectScope();
    scopes.push(scope);
    const storage = scope.run(() =>
      useLocalStorage("migration-options", {
        showEta: new BooleanOption(true),
        theme: new EnumOption("Default", ["Default", "Light"]),
        volume: new RangeOption(50, 0, 100),
      }),
    )!;

    expect(storage.value.showEta.value).toBe(false);
    expect(storage.value.theme.value).toBe("Light");
    expect(storage.value.volume.value).toBe(50);
    expect(storage.value).not.toHaveProperty("obsolete");
    expect(storage.value.showEta.isActive()).toBe(true);
    storage.value.volume.set(25);
    await nextTick();

    const saved = JSON.parse(localStorage.getItem("migration-options")!);
    expect(saved.volume.value).toBe(25);
    expect(saved.volume).not.toHaveProperty("transient");
    expect(storage.value.volume.transient).toEqual({ min: 0, max: 100 });
  });

  test("shares a single reactive persisted state across store consumers", async () => {
    setActivePinia(createPinia());
    const scope = effectScope();
    scopes.push(scope);
    const useStore = defineStore("migration-options", () => ({
      state: useLocalStorage("migration-shared", {
        showEta: new BooleanOption(true),
      }),
    }));
    const first = scope.run(() => useStore())!;
    const second = useStore();

    expect(first).toBe(second);
    first.state.showEta.set(false);
    expect(second.state.showEta.value).toBe(false);
    await nextTick();
    expect(
      JSON.parse(localStorage.getItem("migration-shared")!).showEta.value,
    ).toBe(false);
    first.$dispose();
  });
});
