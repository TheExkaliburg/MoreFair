package de.kaliburg.morefair.utils;

import de.kaliburg.morefair.FairController;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class UpgradeUtils {
    public static BigInteger buyUpgradeCost(Integer ladderNum, Integer currentUpgrade) {
        BigInteger ladder = BigInteger.valueOf(ladderNum + 1);
        BigInteger result = ladder.pow(currentUpgrade + 1);

        return result;
    }

    public static BigInteger throwVinegarCost(Integer ladderNum) {
        return FairController.BASE_VINEGAR_NEEDED_TO_THROW.multiply(BigInteger.valueOf(ladderNum));
    }

    public static BigInteger buyAutoPromoteCost(Integer rank, Integer ladderNum) {
        Integer minPeople = Math.max(FairController.MINIMUM_PEOPLE_FOR_PROMOTE, ladderNum);
        Integer divisor = Math.max(rank - minPeople + 1, 1);

        BigDecimal decGrapes = new BigDecimal(FairController.BASE_GRAPES_NEEDED_TO_AUTO_PROMOTE);
        return decGrapes.divide(BigDecimal.valueOf(divisor), RoundingMode.FLOOR).toBigInteger();
    }
}
