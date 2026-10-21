package de.kaliburg.morefair.game.ladder.model;

import java.util.Set;
import lombok.Getter;

/**
 * The different types of ladders with their order.
 */
@Getter
public enum LadderType implements Comparable<LadderType> {
  DEFAULT,
  // Point Requirements
  TINY,
  SMALL,
  BIG,
  GIGANTIC,
  // Auto-promote
  FREE_AUTO,
  NO_AUTO,
  // Cost of Upgrade
  CHEAP,
  EXPENSIVE,
  // Bottom Grapes
  BOUNTIFUL,
  LAVA,
  // Passing Grapes
  CONSOLATION,
  VIRUS,
  // Winning Prices
  GENEROUS,
  TAXES,
  // End of the Game
  ASSHOLE,
  END,
  VOID(END);

  private final Set<LadderType> impliedTypes;

  LadderType(LadderType... impliedTypes) {
    this.impliedTypes = Set.of(impliedTypes);
  }

  public boolean matches(LadderType requested) {
    return this == requested || impliedTypes.contains(requested);
  }

  /**
   * The order of the ladder types when sorted (ordinal is originally used if the priority is the
   * same).
   */
  private final int priority = 0;

  /**
   * A comparator for comparing instances of {@link LadderType}. The comparison is primarily based
   * on the priority value of each {@code LadderType}. If two {@code LadderType} instances have the
   * same priority, their natural order (based on the {@code compareTo} method of
   * {@link LadderType}) is used to determine the comparison result.
   */
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
