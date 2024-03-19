package de.kaliburg.morefair.game.round_old;

import de.kaliburg.morefair.utils.EnableLoggingPropertiesBeforeAll;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableLoggingPropertiesBeforeAll
public class LadderEntityTest {

/*
  public static Stream<Arguments> constructor_WithReverseScalingRound_ExpectingExpensiveVinegarCost() {
    return Stream.of(
        Arguments.of(1, 5000000),
        Arguments.of(10, 500000)
    );
  }

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

  @ParameterizedTest
  @MethodSource
  void constructor_WithReverseScalingRound_ExpectingExpensiveVinegarCost(int ladderNumber,
      int expected) {
    FairConfig fairConfig = new FairConfig();
    RoundEntity round = new RoundEntity(1, fairConfig);
    round.setTypes(EnumSet.of(RoundType.REVERSE_SCALING));
    round.setBaseAssholeLadder(10);
    round.setHighestAssholeCount(0);

    UpgradeUtils upgradeUtils = new UpgradeUtils(fairConfig, null);
    RoundUtils roundUtils = new RoundUtils();
    LadderUtilsServiceImpl ladderUtils = new LadderUtilsServiceImpl(upgradeUtils, fairConfig);
    upgradeUtils.setLadderUtils(ladderUtils);

    var out = new LadderEntity(ladderNumber, round);
    out.setTypes(EnumSet.of(LadderType.DEFAULT));

    BigInteger vinegarCost = upgradeUtils.throwVinegarCost(out.getScaling());
    assertThat(vinegarCost).isEqualTo(expected);
  }

  @Test
  void constructor_WithRound100_ExpectingCorrectLadderTypes() {
    FairConfig fairConfig = new FairConfig();
    RoundEntity round = new RoundEntity(100, fairConfig);

    assertThat(new LadderEntity(1, round).getTypes()).containsExactlyInAnyOrder(LadderType.GIGANTIC,
        LadderType.CHEAP);
    assertThat(new LadderEntity(2, round).getTypes()).containsExactlyInAnyOrder(LadderType.TINY);
    assertThat(new LadderEntity(10, round).getTypes()).containsExactlyInAnyOrder(
        LadderType.GIGANTIC, LadderType.CHEAP);
    assertThat(new LadderEntity(30, round).getTypes()).containsExactlyInAnyOrder(
        LadderType.GIGANTIC, LadderType.CHEAP);
    assertThat(new LadderEntity(60, round).getTypes()).containsExactlyInAnyOrder(
        LadderType.GIGANTIC, LadderType.CHEAP);
    assertThat(new LadderEntity(50, round).getTypes()).containsExactlyInAnyOrder(
        LadderType.GIGANTIC, LadderType.CHEAP, LadderType.NO_AUTO);
    assertThat(new LadderEntity(53, round).getTypes()).containsExactlyInAnyOrder(LadderType.TINY);
    assertThat(new LadderEntity(57, round).getTypes()).containsExactlyInAnyOrder(LadderType.TINY);
    assertThat(new LadderEntity(61, round).getTypes()).containsExactlyInAnyOrder(LadderType.SMALL);
    assertThat(new LadderEntity(62, round).getTypes()).containsExactlyInAnyOrder(LadderType.SMALL);
    assertThat(new LadderEntity(63, round).getTypes()).containsExactlyInAnyOrder(LadderType.SMALL);
    assertThat(new LadderEntity(64, round).getTypes()).containsExactlyInAnyOrder(LadderType.SMALL);
    assertThat(new LadderEntity(65, round).getTypes()).containsExactlyInAnyOrder(LadderType.SMALL);
    assertThat(new LadderEntity(66, round).getTypes()).containsExactlyInAnyOrder(LadderType.SMALL);
    assertThat(new LadderEntity(67, round).getTypes()).containsExactlyInAnyOrder(LadderType.SMALL);
    assertThat(new LadderEntity(68, round).getTypes()).containsExactlyInAnyOrder(LadderType.SMALL);
    assertThat(new LadderEntity(69, round).getTypes()).containsExactlyInAnyOrder(LadderType.SMALL);
    assertThat(new LadderEntity(70, round).getTypes()).containsExactlyInAnyOrder(
        LadderType.GIGANTIC, LadderType.CHEAP);
    assertThat(new LadderEntity(77, round).getTypes()).containsExactlyInAnyOrder(LadderType.TINY);
    assertThat(new LadderEntity(80, round).getTypes()).containsExactlyInAnyOrder(
        LadderType.GIGANTIC, LadderType.CHEAP);
    assertThat(new LadderEntity(89, round).getTypes()).containsExactlyInAnyOrder(LadderType.TINY);
    assertThat(new LadderEntity(90, round).getTypes()).containsExactlyInAnyOrder(LadderType.TINY);
    assertThat(new LadderEntity(91, round).getTypes()).containsExactlyInAnyOrder(LadderType.TINY);
    assertThat(new LadderEntity(92, round).getTypes()).containsExactlyInAnyOrder(LadderType.SMALL);
    assertThat(new LadderEntity(93, round).getTypes()).containsExactlyInAnyOrder(
        LadderType.DEFAULT);
    assertThat(new LadderEntity(94, round).getTypes()).containsExactlyInAnyOrder(LadderType.BIG);
    assertThat(new LadderEntity(95, round).getTypes()).containsExactlyInAnyOrder(
        LadderType.GIGANTIC);
    assertThat(new LadderEntity(96, round).getTypes()).containsExactlyInAnyOrder(LadderType.TINY);
    assertThat(new LadderEntity(97, round).getTypes()).containsExactlyInAnyOrder(LadderType.SMALL);
    assertThat(new LadderEntity(98, round).getTypes()).containsExactlyInAnyOrder(
        LadderType.DEFAULT);
    assertThat(new LadderEntity(99, round).getTypes()).containsExactlyInAnyOrder(LadderType.BIG);
    assertThat(new LadderEntity(100, round).getTypes()).containsExactlyInAnyOrder(
        LadderType.GIGANTIC, LadderType.CHEAP, LadderType.NO_AUTO);
    assertThat(new LadderEntity(101, round).getTypes()).containsExactlyInAnyOrder(LadderType.END);
  }


  @Test
  void constructor_WithRound100_Expecting10PeopleRequirementEachLadder() {
    FairConfig fairConfig = new FairConfig();
    RoundEntity round = new RoundEntity(100, fairConfig);

    UpgradeUtils upgradeUtils = new UpgradeUtils(fairConfig, null);
    RoundUtils roundUtils = new RoundUtils();
    LadderUtilsServiceImpl ladderUtils = new LadderUtilsServiceImpl(upgradeUtils, fairConfig);
    upgradeUtils.setLadderUtils(ladderUtils);

    assertThat(ladderUtils.getRequiredRankerCountToUnlock(new LadderEntity(1, round))).isEqualTo(
        10);
    assertThat(ladderUtils.getRequiredRankerCountToUnlock(new LadderEntity(5, round))).isEqualTo(
        10);
    assertThat(ladderUtils.getRequiredRankerCountToUnlock(new LadderEntity(10, round))).isEqualTo(
        10);
    assertThat(ladderUtils.getRequiredRankerCountToUnlock(new LadderEntity(20, round))).isEqualTo(
        10);
    assertThat(ladderUtils.getRequiredRankerCountToUnlock(new LadderEntity(34, round))).isEqualTo(
        10);
    assertThat(ladderUtils.getRequiredRankerCountToUnlock(new LadderEntity(50, round))).isEqualTo(
        10);
    assertThat(ladderUtils.getRequiredRankerCountToUnlock(new LadderEntity(76, round))).isEqualTo(
        10);
    assertThat(ladderUtils.getRequiredRankerCountToUnlock(new LadderEntity(100, round))).isEqualTo(
        10);
  }*/
}
