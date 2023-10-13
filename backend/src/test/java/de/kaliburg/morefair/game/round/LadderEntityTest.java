package de.kaliburg.morefair.game.round;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.UpgradeUtils;
import de.kaliburg.morefair.utils.EnableLoggingPropertiesBeforeAll;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.util.EnumSet;
import java.util.stream.Stream;

@Slf4j
@EnableLoggingPropertiesBeforeAll
public class LadderEntityTest {



    @Test
    void constructor_WithReverseScalingRound_ExpectingReverseScale() {
        FairConfig fairConfig = new FairConfig();
        RoundEntity round = new RoundEntity(1, fairConfig);
        round.setTypes(EnumSet.of(RoundType.REVERSE_SCALING));
        round.setBaseAssholeLadder(10);
        round.setHighestAssholeCount(0);

        Assertions.assertThat(new LadderEntity(1, round).getScaling()).isEqualTo(10);
        Assertions.assertThat(new LadderEntity(3, round).getScaling()).isEqualTo(8);
        Assertions.assertThat(new LadderEntity(5, round).getScaling()).isEqualTo(6);
        Assertions.assertThat(new LadderEntity(7, round).getScaling()).isEqualTo(4);
        Assertions.assertThat(new LadderEntity(10, round).getScaling()).isEqualTo(1);
        Assertions.assertThat(new LadderEntity(100, round).getScaling()).isEqualTo(1);
    }

    public static Stream<Arguments> constructor_WithReverseScalingRound_ExpectingExpensiveVinegarCost() {
        return Stream.of(
                Arguments.of(1, 5000000),
                Arguments.of(10 , 500000)
        );
    }

    @ParameterizedTest
    @MethodSource
    void constructor_WithReverseScalingRound_ExpectingExpensiveVinegarCost(int ladderNumber, int expected) {
        FairConfig fairConfig = new FairConfig();
        RoundEntity round = new RoundEntity(1, fairConfig);
        round.setTypes(EnumSet.of(RoundType.REVERSE_SCALING));
        round.setBaseAssholeLadder(10);
        round.setHighestAssholeCount(0);

        UpgradeUtils upgradeUtils = new UpgradeUtils(fairConfig);

        var out = new LadderEntity(ladderNumber, round);
        out.setTypes(EnumSet.of(LadderType.DEFAULT));

        BigInteger vinegarCost = upgradeUtils.throwVinegarCost(out.getScaling());
        Assertions.assertThat(vinegarCost).isEqualTo(expected);
    }
}
