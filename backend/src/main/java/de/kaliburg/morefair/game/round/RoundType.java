package de.kaliburg.morefair.game.round;

import java.util.Comparator;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public enum RoundType {
  DEFAULT, FAST, SLOW, AUTO, CHAOS(1);

  @Getter
  private int priority = 0;

  RoundType(int priority) {
    this.priority = priority;
  }
}

class RoundTypeComparator implements Comparator<RoundType> {

  @Override
  public int compare(RoundType o1, RoundType o2) {
    if (o1.getPriority() == o2.getPriority()) {
      return o1.compareTo(o2);
    }

    return o2.getPriority() - o1.getPriority();
  }
}
