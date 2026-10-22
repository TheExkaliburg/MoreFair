package de.kaliburg.morefair.game.ladder.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class LadderEntityTest {

  @Test
  void newLadder_isNotReached() {
    LadderEntity ladder = new LadderEntity();

    assertThat(ladder.getFirstReachedOn()).isNull();
    assertThat(ladder.isReached()).isFalse();
  }

  @Test
  void builtLadder_isNotReachedByDefault() {
    LadderEntity ladder = LadderEntity.builder()
        .number(1)
        .roundId(1L)
        .scaling(1)
        .basePointsToPromote(BigInteger.ZERO)
        .build();

    assertThat(ladder.getFirstReachedOn()).isNull();
    assertThat(ladder.isReached()).isFalse();
  }

  @Test
  void ladderWithFirstArrival_isReached() {
    OffsetDateTime firstReachedOn = OffsetDateTime.parse("2026-10-22T14:00:00Z");
    LadderEntity ladder = new LadderEntity().setFirstReachedOn(firstReachedOn);

    assertThat(ladder.getFirstReachedOn()).isEqualTo(firstReachedOn);
    assertThat(ladder.isReached()).isTrue();
  }
}
