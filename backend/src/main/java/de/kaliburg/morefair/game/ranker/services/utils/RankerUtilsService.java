package de.kaliburg.morefair.game.ranker.services.utils;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import java.math.BigInteger;

public interface RankerUtilsService {

  boolean canThrowVinegarAt(RankerEntity ranker, RankerEntity target, int percentage);

  boolean canBuyAutoPromote(RankerEntity ranker);

  boolean canPromote(RankerEntity ranker);

  BigInteger getPointsForPromoteWithLead(RankerEntity ranker);

  BigInteger getWinningGrapes(LadderEntity ladder);
}
