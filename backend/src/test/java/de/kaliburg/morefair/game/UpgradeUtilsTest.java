package de.kaliburg.morefair.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.kaliburg.morefair.game.round.LadderType;
import de.kaliburg.morefair.game.round.UpgradeUtils;
import de.kaliburg.morefair.utils.FairTest;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests for {@link UpgradeUtils}. for specific values in the config look at <a
 * href="https://docs.google.com/spreadsheets/d/1BH1Y-hv_zIho8QIsdxCp8Rmld__zBuviu_wL7Hhtb7s ">this
 * sheet</a>
 */

@Slf4j
@FairTest
class UpgradeUtilsTest {

  @Autowired
  private UpgradeUtils upgradeUtils;

  @Test
  void compareUpgrade1To5_Ladder1() {
    List<BigInteger> expected = convertIntListToBigInt(List.of(1, 2, 4));
    List<BigInteger> actual = getAllUpgradeCosts(1, 0);
    assertEquals(expected, actual);

    expected = convertIntListToBigInt(List.of(1, 4, 9));
    actual = getAllUpgradeCosts(1, 1);
    assertEquals(expected, actual);

    expected = convertIntListToBigInt(List.of(2, 8, 23));
    actual = getAllUpgradeCosts(1, 2);
    assertEquals(expected, actual);

    expected = convertIntListToBigInt(List.of(3, 16, 59));
    actual = getAllUpgradeCosts(1, 3);
    assertEquals(expected, actual);

    expected = convertIntListToBigInt(List.of(4, 32, 146));
    actual = getAllUpgradeCosts(1, 4);
    assertEquals(expected, actual);
  }

  @Test
  void compareUpgrade1To5_Ladder10() {
    List<BigInteger> expected = convertIntListToBigInt(List.of(3, 11, 24));
    List<BigInteger> actual = getAllUpgradeCosts(10, 0);
    assertEquals(expected, actual);

    expected = convertIntListToBigInt(List.of(18, 121, 384));
    actual = getAllUpgradeCosts(10, 1);
    assertEquals(expected, actual);

    expected = convertIntListToBigInt(List.of(108, 1331, 6144));
    actual = getAllUpgradeCosts(10, 2);
    assertEquals(expected, actual);

    expected = convertIntListToBigInt(List.of(648, 14641, 98304));
    actual = getAllUpgradeCosts(10, 3);
    assertEquals(expected, actual);

    expected = convertIntListToBigInt(List.of(3888, 161051, 1572864));
    actual = getAllUpgradeCosts(10, 4);
    assertEquals(expected, actual);
  }


  @Test
  void compareUpgrade1To5_Ladder25() {
    List<BigInteger> expected = convertIntListToBigInt(List.of(7, 26, 58));
    List<BigInteger> actual = getAllUpgradeCosts(25, 0);
    assertEquals(expected, actual);

    expected = convertIntListToBigInt(List.of(91, 676, 2223));
    actual = getAllUpgradeCosts(25, 1);
    assertEquals(expected, actual);

    expected = convertIntListToBigInt(List.of(1230, 17576, 85600));
    actual = getAllUpgradeCosts(25, 2);
    assertEquals(expected, actual);

    expected = convertIntListToBigInt(List.of(16608, 456976, 3295598));
    actual = getAllUpgradeCosts(25, 3);
    assertEquals(expected, actual);

    expected = convertIntListToBigInt(List.of(224202, 11881376, 126880507));
    actual = getAllUpgradeCosts(25, 4);
    assertEquals(expected, actual);
  }

  @Test
  void compareUpgrade1To5_Ladder30() {
    List<BigInteger> expected = convertIntListToBigInt(List.of(8, 31, 69));
    List<BigInteger> actual = getAllUpgradeCosts(30, 0);
    assertEquals(expected, actual);

    expected = convertIntListToBigInt(List.of(128, 961, 3174));
    actual = getAllUpgradeCosts(30, 1);
    assertEquals(expected, actual);

    expected = convertIntListToBigInt(List.of(2048, 29791, 146004));
    actual = getAllUpgradeCosts(30, 2);
    assertEquals(expected, actual);

    expected = convertIntListToBigInt(List.of(32768, 923521, 6716184));
    actual = getAllUpgradeCosts(30, 3);
    assertEquals(expected, actual);

    expected = convertIntListToBigInt(List.of(524288, 28629151, 308944464));
    actual = getAllUpgradeCosts(30, 4);
    assertEquals(expected, actual);
  }

  private List<BigInteger> getAllUpgradeCosts(int ladderNumber, int currentUpgrade) {
    List<BigInteger> costs = new ArrayList<>();
    costs.add(
        upgradeUtils.buyUpgradeCost(ladderNumber, currentUpgrade, EnumSet.of(LadderType.CHEAP)));
    costs.add(upgradeUtils.buyUpgradeCost(ladderNumber, currentUpgrade));
    costs.add(
        upgradeUtils.buyUpgradeCost(ladderNumber, currentUpgrade,
            EnumSet.of(LadderType.EXPENSIVE)));
    return costs;
  }

  private List<BigInteger> convertIntListToBigInt(List<Integer> intList) {
    List<BigInteger> bigIntList = new ArrayList<>();
    intList.forEach(i -> bigIntList.add(BigInteger.valueOf(i)));
    return bigIntList;
  }
}