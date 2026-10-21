export enum LadderType {
  DEFAULT = "DEFAULT",
  // Point Requirements
  TINY = "TINY",
  SMALL = "SMALL",
  BIG = "BIG",
  GIGANTIC = "GIGANTIC",
  // Auto-promote
  FREE_AUTO = "FREE_AUTO",
  NO_AUTO = "NO_AUTO",
  // Cost of Upgrade
  CHEAP = "CHEAP",
  EXPENSIVE = "EXPENSIVE",
  // Bottom Grapes
  BOUNTIFUL = "BOUNTIFUL",
  LAVA = "LAVA",
  // Passing Grapes
  CONSOLATION = "CONSOLATION",
  VIRUS = "VIRUS",
  // Winning Prices
  GENEROUS = "GENEROUS",
  TAXES = "TAXES",
  // End of the Game
  ASSHOLE = "ASSHOLE",
  END = "END",
  VOID = "VOID",
}

const impliedLadderTypes: Partial<Record<LadderType, readonly LadderType[]>> = {
  [LadderType.VOID]: [LadderType.END],
};

export function hasLadderType(
  types: ReadonlySet<LadderType>,
  requested: LadderType,
): boolean {
  return Array.from(types).some(
    (type) =>
      type === requested ||
      impliedLadderTypes[type]?.includes(requested) === true,
  );
}
