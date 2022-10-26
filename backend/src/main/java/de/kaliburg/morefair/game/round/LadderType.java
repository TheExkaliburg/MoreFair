package de.kaliburg.morefair.game.round;

import java.util.Comparator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public enum LadderType implements Comparable<LadderType> {
  DEFAULT, TINY, SMALL, BIG, GIGANTIC, FREE_AUTO, NO_AUTO, ASSHOLE;

  @Getter
  private int priority = 0;
}

class LadderTypeComparator implements Comparator<LadderType> {

  @Override
  public int compare(LadderType o1, LadderType o2) {
    if (o1.getPriority() == o2.getPriority()) {
      return o1.compareTo(o2);
    }

    return o2.getPriority() - o1.getPriority();
  }
}
