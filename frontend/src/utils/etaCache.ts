import { ref } from "vue";

// This is only separate because of circular dependencies, so we can import it in useEta.ts without issues
export const etaRankerCache = new Map<number, Map<number, number>>();
export const etaPointsCache = new Map<number, Map<string, number>>();
export const etaPowerCache = new Map<number, Map<string, number>>();
export const computedTrigger = ref(0);

export function resetEtaCache() {
  etaRankerCache.clear();
  etaPointsCache.clear();
  etaPowerCache.clear();
  computedTrigger.value = 1 - computedTrigger.value;
}
