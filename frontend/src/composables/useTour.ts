import introJs from "intro.js";
// @ts-ignore
import { navigateTo } from "nuxt/app";
import { RemovableRef } from "@vueuse/core";
import { useUiStore } from "~/store/ui";
import { useLang } from "~/composables/useLang";

export type TourFlags = {
  isOpen: boolean;
  shownStartup: boolean;
  shownBiased: boolean;
  shownMultied: boolean;
  shownAutoPromote: boolean;
  shownVinegar: boolean;
  shownPromoted: boolean;
};

const defaultValues: TourFlags = {
  isOpen: false,
  shownStartup: false,
  shownBiased: false,
  shownMultied: false,
  shownAutoPromote: false,
  shownVinegar: false,
  shownPromoted: false,
};

const flags: RemovableRef<TourFlags> = useLocalStorage("flags", defaultValues);
flags.value.isOpen = false;

export const useStartupTour = () => {
  const lang = useLang("tour.startup");
  const tour = introJs();

  function start() {
    if (flags.value.isOpen) return;
    navigateTo("/");
    const steps: introJs.Step[] = [
      {
        title: lang("welcome.title"),
        intro: lang("welcome.intro"),
      },
      {
        element: document.querySelector(".ranker-you .points") || undefined,
        title: lang("points.title"),
        intro: lang("points.intro"),
      },
      {
        element: document.querySelector(".ranker-you .power") || undefined,
        title: lang("power.title"),
        intro: lang("power.intro"),
      },
      {
        element: document.querySelector("[data-tutorial='bias']") || undefined,
        title: lang("bias.title"),
        intro: lang("bias.intro"),
      },
    ];

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
          setTimeout(resolve, 150);
        } else {
          resolve();
        }
      });
    });
    tour.oncomplete(() => {
      flags.value.shownStartup = true;
      flags.value.isOpen = false;
    });
    tour.onexit(() => {
      flags.value.shownStartup = true;
      flags.value.isOpen = false;
    });
    tour.start();
    flags.value.isOpen = true;
  }

  return {
    start,
    flags,
  };
};

export const useBiasedTour = () => {
  const lang = useLang("tour.biased");
  const tour = introJs();

  function start() {
    if (flags.value.isOpen) return;
    navigateTo("/");
    const steps: introJs.Step[] = [
      {
        element: document.querySelector(".ranker-you .rank") || undefined,
        title: lang("rank.title"),
        intro: lang("rank.intro"),
      },
      {
        element:
          document.querySelector(".ranker-you .formattedPowerPerSec") ||
          undefined,
        title: lang("power.title"),
        intro: lang("power.intro"),
      },
      {
        element: document.querySelector("[data-tutorial='multi']") || undefined,
        title: lang("multi.title"),
        intro: lang("multi.intro"),
      },
    ];

    tour.setOptions({
      steps,
      exitOnOverlayClick: false,
      exitOnEsc: false,
      nextLabel: "Next",
      prevLabel: "Back",
      doneLabel: "Done",
    });
    tour.oncomplete(() => {
      flags.value.shownBiased = true;
      flags.value.isOpen = false;
    });
    tour.onexit(() => {
      flags.value.shownBiased = true;
      flags.value.isOpen = false;
    });
    tour.start();
    flags.value.isOpen = true;
  }

  return {
    start,
    flags,
  };
};

export const useMultiedTour = () => {
  const lang = useLang("tour.multied");
  const tour = introJs();

  function start() {
    if (flags.value.isOpen) return;
    navigateTo("/");
    const steps: introJs.Step[] = [
      {
        element: document.querySelector(".ranker-you") || undefined,
        title: lang("tip.title"),
        intro: lang("tip.intro"),
      },
      {
        element: document.querySelector(".ranker-1 .rank") || undefined,
        title: lang("goal1.title"),
        intro: lang("goal1.intro"),
      },
      {
        element:
          document.querySelector("[data-tutorial='ladders']") || undefined,
        title: lang("goal2.title"),
        intro: lang("goal2.intro"),
      },
    ];

    tour.setOptions({
      steps,
      exitOnOverlayClick: false,
      exitOnEsc: false,
      nextLabel: "Next",
      prevLabel: "Back",
      doneLabel: "Done",
    });
    tour.oncomplete(() => {
      flags.value.shownMultied = true;
      flags.value.isOpen = false;
    });
    tour.onexit(() => {
      flags.value.shownMultied = true;
      flags.value.isOpen = false;
    });
    tour.start();
    flags.value.isOpen = true;
  }

  return {
    start,
    flags,
  };
};

export const useAutoPromoteTour = () => {
  const lang = useLang("tour.autoPromote");
  const tour = introJs();

  function start() {
    if (flags.value.isOpen) return;
    navigateTo("/");
    const steps: introJs.Step[] = [
      {
        element: document.querySelector(".ranker-1") || undefined,
        title: lang("shootdown.title"),
        intro: lang("shootdown.intro"),
      },
      {
        element:
          document.querySelector("[data-tutorial='grapes']") || undefined,
        title: lang("grapes.title"),
        intro: lang("grapes.intro"),
      },
      {
        element:
          document.querySelector("[data-tutorial='autoPromote']") || undefined,
        title: lang("autoPromote.title"),
        intro: lang("autoPromote.intro"),
      },
      {
        element:
          document.querySelector("[data-tutorial='autoPromoteCost']") ||
          undefined,
        title: lang("cost.title"),
        intro: lang("cost.intro"),
      },
    ];

    tour.setOptions({
      steps,
      exitOnOverlayClick: false,
      exitOnEsc: false,
      nextLabel: "Next",
      prevLabel: "Back",
      doneLabel: "Done",
    });
    tour.oncomplete(() => {
      flags.value.shownAutoPromote = true;
      flags.value.isOpen = false;
    });
    tour.onexit(() => {
      flags.value.shownAutoPromote = true;
      flags.value.isOpen = false;
    });
    tour.start();
    flags.value.isOpen = true;
  }

  return {
    start,
    flags,
  };
};

export const useVinegarTour = () => {
  const lang = useLang("tour.vinegar");
  const tour = introJs();

  function start() {
    if (flags.value.isOpen) return;
    navigateTo("/");
    const steps: introJs.Step[] = [
      {
        title: lang("split.title"),
        intro: lang("split.intro"),
      },
      {
        element:
          document.querySelector("[data-tutorial='throwVinegar']") || undefined,
        title: lang("throwing.title"),
        intro: lang("throwing.intro"),
      },
      {
        element: document.querySelector("[data-tutorial='wine']") || undefined,
        title: lang("wine.title"),
        intro: lang("wine.intro"),
      },
      {
        element:
          document.querySelector("[data-tutorial='vinegarSettings']") ||
          undefined,
        title: lang("refunds.title"),
        intro: lang("refunds.intro"),
      },
    ];

    tour.setOptions({
      steps,
      exitOnOverlayClick: false,
      exitOnEsc: false,
      nextLabel: "Next",
      prevLabel: "Back",
      doneLabel: "Done",
    });
    tour.oncomplete(() => {
      flags.value.shownVinegar = true;
      flags.value.isOpen = false;
    });
    tour.onexit(() => {
      flags.value.shownVinegar = true;
      flags.value.isOpen = false;
    });
    tour.start();
    flags.value.isOpen = true;
  }

  return {
    start,
    flags,
  };
};

export const usePromoteTour = () => {
  const lang = useLang("tour.promote");
  const tour = introJs();

  function start() {
    navigateTo("/");
    const steps: introJs.Step[] = [
      {
        title: lang("top10.title"),
        intro: lang("top10.intro"),
      },
      {
        title: lang("balance.title"),
        intro: lang("balance.intro"),
      },
    ];

    tour.setOptions({
      steps,
      exitOnOverlayClick: false,
      exitOnEsc: false,
      nextLabel: "Next",
      prevLabel: "Back",
      doneLabel: "Done",
    });
    tour.oncomplete(() => {
      flags.value.shownPromoted = true;
      flags.value.isOpen = false;
    });
    tour.onexit(() => {
      flags.value.shownPromoted = true;
      flags.value.isOpen = false;
    });
    tour.start();
    flags.value.isOpen = true;
  }

  return {
    start,
    flags,
  };
};

(window as any).tours = {
  useStartupTour,
  useBiasedTour,
  useMultiedTour,
  useAutoPromoteTour,
  useVinegarTour,
  usePromoteTour,
};
