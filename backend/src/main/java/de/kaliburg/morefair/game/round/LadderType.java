package de.kaliburg.morefair.game.round;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The different types of ladders with their order.
 */
@NoArgsConstructor
@AllArgsConstructor
public enum LadderType implements Comparable<LadderType> {
  DEFAULT, TINY, SMALL, BIG, GIGANTIC, FREE_AUTO, NO_AUTO, ASSHOLE, CHEAP, CHEAP_2, CHEAP_3, CHEAP_4, CHEAP_5, CHEAP_6, CHEAP_7, CHEAP_8, CHEAP_9, CHEAP_10, EXPENSIVE, END;

  /**
   * The order of the ladder types when sorted (ordinal is originally used if the priority is the
   * same).
   */
  @Getter
  private int priority = 0;
}


