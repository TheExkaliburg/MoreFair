package de.kaliburg.morefair.game.ladder.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The different types of ladders with their order.
 */
@Getter
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
  private final int priority = 0;

  public static class Comparator implements java.util.Comparator<LadderType> {

    @Override
    public int compare(LadderType o1, LadderType o2) {
      if (o1.getPriority() == o2.getPriority()) {
        return o1.compareTo(o2);
      }

      return o2.getPriority() - o1.getPriority();
    }
  }
}


