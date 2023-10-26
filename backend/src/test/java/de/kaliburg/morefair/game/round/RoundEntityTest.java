package de.kaliburg.morefair.game.round;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.utils.EnableLoggingPropertiesBeforeAll;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@EnableLoggingPropertiesBeforeAll
class RoundEntityTest {

    @Test
    void constructor_WithRound100_ExpectingCorrectRoundTypes() {
        FairConfig fairConfig = new FairConfig();

        assertThat(new RoundEntity(100, fairConfig).getTypes())
                .isEqualTo(EnumSet.of(RoundType.SPECIAL_100, RoundType.REVERSE_SCALING));
        assertThat(new RoundEntity(200, fairConfig).getTypes())
                .isEqualTo(EnumSet.of(RoundType.SPECIAL_100, RoundType.REVERSE_SCALING));
        assertThat(new RoundEntity(300, fairConfig).getTypes())
                .isEqualTo(EnumSet.of(RoundType.SPECIAL_100, RoundType.REVERSE_SCALING));
    }

    @Test
    void constructor_WithRound100_Expecting100Ladders() {
        FairConfig fairConfig = new FairConfig();

        assertThat(new RoundEntity(100, fairConfig).getAssholeLadderNumber()).isEqualTo(100);
    }
}