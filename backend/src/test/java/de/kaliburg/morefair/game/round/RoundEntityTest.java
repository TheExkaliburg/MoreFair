package de.kaliburg.morefair.game.round;

import static org.assertj.core.api.Assertions.assertThat;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.utils.EnableLoggingPropertiesBeforeAll;
import java.util.EnumSet;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
@EnableLoggingPropertiesBeforeAll
class RoundEntityTest {

  @Test
  void constructor_WithRound200_ExpectingCorrectRoundTypes() {
    FairConfig fairConfig = new FairConfig();

    assertThat(new RoundEntity(100, fairConfig).getTypes())
        .isNotEqualTo(EnumSet.of(RoundType.SPECIAL_100, RoundType.REVERSE_SCALING));
    assertThat(new RoundEntity(200, fairConfig).getTypes())
        .isEqualTo(EnumSet.of(RoundType.SPECIAL_100, RoundType.REVERSE_SCALING));
    assertThat(new RoundEntity(300, fairConfig).getTypes())
        .isNotEqualTo(EnumSet.of(RoundType.SPECIAL_100, RoundType.REVERSE_SCALING));
  }

  @Test
  void constructor_WithRound200_Expecting100Ladders() {
    FairConfig fairConfig = new FairConfig();

    assertThat(new RoundEntity(200, fairConfig).getAssholeLadderNumber()).isEqualTo(100);
  }

  @Test
  void constructor_WithRound300_ExpectingCorrectRoundTypes() {
    FairConfig fairConfig = new FairConfig();
    assertThat(new RoundEntity(300, fairConfig).getTypes())
        .isEqualTo(EnumSet.of(RoundType.CHAOS));

    assertThat(new RoundEntity(301, fairConfig).getTypes())
        .isEqualTo(EnumSet.of(RoundType.DEFAULT));
  }
}