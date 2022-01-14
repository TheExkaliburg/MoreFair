package de.kaliburg.morefair.utils;

import de.kaliburg.morefair.controller.FairController;

import java.math.BigInteger;

public class UpgradeUtils {
    public static BigInteger buyUpgradeCost(Integer ladderNum, Integer currentUpgrade) {
        return BigInteger.valueOf(ladderNum + 1).pow(currentUpgrade);
    }

    public static BigInteger throwVinegarCost(Integer ladderNum) {
        return FairController.BASE_VINEGAR_NEEDED_TO_THROW.multiply(BigInteger.valueOf(ladderNum));
    }
}
