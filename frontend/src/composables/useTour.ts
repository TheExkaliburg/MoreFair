import introJs from "intro.js";
import { useStorage } from "@vueuse/core";
import { deepMerge } from "@antfu/utils";
import { navigateTo } from "nuxt/app";
import { useUiStore } from "~/store/ui";

const defaultValues = {
  showHelp: false,
};

export function useTutorialTour() {
  const tour = introJs();
  const steps: introJs.Step[] = [
    {
      intro: "Welcome to the tutorial!",
    },
    {
      element: document.querySelector("[data-tutorial='help']") || undefined,
      title: "This was it!",
      intro:
        "If you ever need this tutorial again, just click this help button again.",
    },
  ];

  const initialValues = {};
  Object.assign(initialValues, defaultValues);
  const flags: any = useStorage("tutorial", initialValues, localStorage, {
    mergeDefaults: (storageValue, defaultValue) =>
      deepMerge(defaultValue, storageValue),
  });

  function start() {
    navigateTo("/");
    tour.setOptions({
      steps,
      exitOnOverlayClick: false,
      exitOnEsc: false,
      nextLabel: "Next",
      prevLabel: "Back",
      doneLabel: "Done",
    });
    tour.onbeforechange((targetElement) => {
      return new Promise<void>((resolve) => {
        if (targetElement.dataset.tutorial === "help") {
          useUiStore().state.sidebarExpanded = true;
          setTimeout(() => resolve(), 150);
        } else {
          resolve();
        }
      });
    });
    tour.oncomplete(() => {
      flags.value.showHelp = true;
    });
    tour.start();
  }

  function getFlag() {
    return flags.value.showHelp;
  }

  return {
    start,
    getFlag,
  };
}
