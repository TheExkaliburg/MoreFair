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

import static org.assertj.core.api.Assertions.*;

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

        assertThat(new LadderEntity(1, round).getScaling()).isEqualTo(10);
        assertThat(new LadderEntity(3, round).getScaling()).isEqualTo(8);
        assertThat(new LadderEntity(5, round).getScaling()).isEqualTo(6);
        assertThat(new LadderEntity(7, round).getScaling()).isEqualTo(4);
        assertThat(new LadderEntity(10, round).getScaling()).isEqualTo(1);
        assertThat(new LadderEntity(100, round).getScaling()).isEqualTo(1);
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
        assertThat(vinegarCost).isEqualTo(expected);
    }

    @Test
    void constructor_WithRound100_ExpectingCorrectLadderTypes() {
        FairConfig fairConfig = new FairConfig();
        RoundEntity round = new RoundEntity(100, fairConfig);

        assertThat(new LadderEntity(1, round).getTypes()).containsExactlyInAnyOrder(LadderType.NO_AUTO, LadderType.TINY);
        assertThat(new LadderEntity(2, round).getTypes()).containsExactlyInAnyOrder(LadderType.TINY);
        assertThat(new LadderEntity(53, round).getTypes()).containsExactlyInAnyOrder(LadderType.TINY);
        assertThat(new LadderEntity(77, round).getTypes()).containsExactlyInAnyOrder(LadderType.TINY);
        assertThat(new LadderEntity(10, round).getTypes()).containsExactlyInAnyOrder(LadderType.GIGANTIC, LadderType.CHEAP);
        assertThat(new LadderEntity(30, round).getTypes()).containsExactlyInAnyOrder(LadderType.GIGANTIC, LadderType.CHEAP);
        assertThat(new LadderEntity(80, round).getTypes()).containsExactlyInAnyOrder(LadderType.GIGANTIC, LadderType.CHEAP);
        assertThat(new LadderEntity(50, round).getTypes()).containsExactlyInAnyOrder(LadderType.GIGANTIC, LadderType.CHEAP, LadderType.NO_AUTO);
        assertThat(new LadderEntity(100, round).getTypes()).containsExactlyInAnyOrder(LadderType.GIGANTIC, LadderType.CHEAP, LadderType.NO_AUTO, LadderType.ASSHOLE);
    }


    @Test
    void constructor_WithRound100_Expecting10PeopleRequirementEachLadder() {
        FairConfig fairConfig = new FairConfig();
        RoundEntity round = new RoundEntity(100, fairConfig);

        UpgradeUtils upgradeUtils = new UpgradeUtils(fairConfig);
        RoundUtils roundUtils = new RoundUtils(fairConfig);
        LadderUtils ladderUtils = new LadderUtils(upgradeUtils, roundUtils, fairConfig);

        assertThat(ladderUtils.getRequiredRankerCountToUnlock(new LadderEntity(1, round))).isEqualTo(10);
        assertThat(ladderUtils.getRequiredRankerCountToUnlock(new LadderEntity(5, round))).isEqualTo(10);
        assertThat(ladderUtils.getRequiredRankerCountToUnlock(new LadderEntity(10, round))).isEqualTo(10);
        assertThat(ladderUtils.getRequiredRankerCountToUnlock(new LadderEntity(20, round))).isEqualTo(10);
        assertThat(ladderUtils.getRequiredRankerCountToUnlock(new LadderEntity(34, round))).isEqualTo(10);
        assertThat(ladderUtils.getRequiredRankerCountToUnlock(new LadderEntity(50, round))).isEqualTo(10);
        assertThat(ladderUtils.getRequiredRankerCountToUnlock(new LadderEntity(76, round))).isEqualTo(10);
        assertThat(ladderUtils.getRequiredRankerCountToUnlock(new LadderEntity(100, round))).isEqualTo(10);
    }
}
