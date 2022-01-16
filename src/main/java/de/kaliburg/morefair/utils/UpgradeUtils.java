package de.kaliburg.morefair.utils;

import de.kaliburg.morefair.controller.FairController;

import java.math.BigInteger;

public class UpgradeUtils {
    public static BigInteger buyUpgradeCost(Integer ladderNum, Integer currentUpgrade) {
        BigInteger ladder = BigInteger.valueOf(ladderNum + 1);
        BigInteger result = ladder.pow(currentUpgrade);

        return result;
    }

    public static BigInteger throwVinegarCost(Integer ladderNum) {
        return FairController.BASE_VINEGAR_NEEDED_TO_THROW.multiply(BigInteger.valueOf(ladderNum));
    }
}
