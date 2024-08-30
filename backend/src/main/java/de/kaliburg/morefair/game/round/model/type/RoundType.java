package de.kaliburg.morefair.game.round.model.type;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public enum RoundType {
  DEFAULT,
  // Speed -> Point Requirements
  FAST,
  SLOW,
  // Ladders Per Round
  SHORT,
  LONG,
  // AutoPromote enabled
  AUTO,
  // more Randomness
  CHAOS(1),
  // Vinegar Modifiers
  RAILROAD(2),
  FARMER(2),
  RACE(2),
  // Unused
  REVERSE_SCALING,
  SPECIAL_100;

  private int priority = 0;

  RoundType(int priority) {
    this.priority = priority;
  }

  /**
   * Lets the higher Priority get handled first.
   */
  public static class Comparator implements java.util.Comparator<RoundType> {

    @Override
    public int compare(RoundType o1, RoundType o2) {
      if (o1.getPriority() == o2.getPriority()) {
        return o1.compareTo(o2);
      }

      return o2.getPriority() - o1.getPriority();
    }
  }
}


