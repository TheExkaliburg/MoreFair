package de.kaliburg.morefair.game.ladder.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The different types of ladders with their order.
 */
@NoArgsConstructor
@AllArgsConstructor
public enum LadderType implements Comparable<LadderType> {
  DEFAULT,
  TINY,
  SMALL,
  BIG,
  GIGANTIC,
  FREE_AUTO,
  NO_AUTO,
  ASSHOLE,
  CHEAP,
  EXPENSIVE,
  BOUNTIFUL,
  DROUGHT,
  CONSOLATION,
  NO_HANDOUTS,
  GENEROUS,
  STINGY,
  END;

  /**
   * The order of the ladder types when sorted (ordinal is originally used if the priority is the
   * same).
   */
  @Getter
  private int priority = 0;
}


