import { vi } from "vitest";

const uiEffects = vi.hoisted(() => ({
  notify: vi.fn(),
  playSound: vi.fn(),
  startTour: vi.fn(),
}));

vi.mock("~/composables/useToasts", () => ({
  useToasts: uiEffects.notify,
}));

vi.mock("~/composables/useSound", () => ({
  SOUNDS: {},
  useSound: () => ({ play: uiEffects.playSound }),
}));

vi.mock("~/composables/useTour", () => {
  const disabledTour = () => ({
    flags: {
      value: {
        shownStartup: true,
        shownBiased: true,
        shownMultied: true,
        shownAutoPromote: true,
        shownVinegar: true,
        shownPromoted: true,
      },
    },
    start: uiEffects.startTour,
  });
  return {
    useStartupTour: disabledTour,
    useBiasedTour: disabledTour,
    useMultiedTour: disabledTour,
    useAutoPromoteTour: disabledTour,
    useVinegarTour: disabledTour,
    usePromoteTour: disabledTour,
  };
});

export function resetUiEffects() {
  uiEffects.notify.mockReset();
  uiEffects.playSound.mockReset();
  uiEffects.startTour.mockReset();
}
