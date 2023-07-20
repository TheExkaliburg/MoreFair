import introJs from "intro.js";
import { useStorage } from "@vueuse/core";
// @ts-ignore
import { deepMerge } from "@antfu/utils";
import { navigateTo } from "nuxt/app";
import { useUiStore } from "~/store/ui";
import { useLang } from "~/composables/useLang";

const defaultValues = {
  showHelp: false,
};

export const useTutorialTour = () => {
  const lang = useLang("tour.tutorial");
  const tour = introJs();

  navigateTo("/");
  const steps: introJs.Step[] = [
    {
      title: lang("welcome.title"),
      intro: lang("welcome.intro"),
    },
    {
      element: document.querySelector(".ranker-1") || undefined,
      title: lang("goal1.title"),
      intro: lang("goal1.intro"),
      position: "bottom",
    },
    {
      element: document.querySelector("[data-tutorial='info']") || undefined,
      title: lang("goal2.title"),
      intro: lang("goal2.intro"),
      position: "bottom",
    },
    {
      element: document.querySelector(".ranker-1 .points") || undefined,
      title: lang("points.title"),
      intro: lang("points.intro"),
      position: "left",
    },
    {
      element: document.querySelector(".ranker-you .power") || undefined,
      title: lang("power.title"),
      intro: lang("power.intro"),
      position: "left",
    },
    {
      element: document.querySelector("[data-tutorial='bias']") || undefined,
      title: lang("bias.title"),
      intro: lang("bias.intro"),
      position: "top",
    },
    {
      element: document.querySelector("[data-tutorial='multi']") || undefined,
      title: lang("multi.title"),
      intro: lang("multi.intro"),
      position: "top",
    },
    {
      element: document.querySelector("[data-tutorial='vinegar']") || undefined,
      title: lang("vinegar.title"),
      intro: lang("vinegar.intro"),
      position: "top",
    },
    {
      element: document.querySelector("[data-tutorial='grapes']") || undefined,
      title: lang("grapes.title"),
      intro: lang("grapes.intro"),
      position: "top",
    },
    {
      element: document.querySelector("[data-tutorial='help']") || undefined,
      title: lang("end.title"),
      intro: lang("end.intro"),
      position: "top",
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
    tour.onexit(() => {
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
};
