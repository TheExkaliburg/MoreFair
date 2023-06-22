package de.kaliburg.morefair.game.round;

import java.util.Comparator;

/**
 * Comparator to sort the LadderType by priority (highest first).
 */
class LadderTypeComparator implements Comparator<LadderType> {

  @Override
  public int compare(LadderType o1, LadderType o2) {
    if (o1.getPriority() == o2.getPriority()) {
      return o1.compareTo(o2);
    }

    return o2.getPriority() - o1.getPriority();
  }
}
