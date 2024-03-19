package de.kaliburg.morefair.game.round.services.utils;

import de.kaliburg.morefair.game.round.model.RoundEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoundUtilsServiceImpl implements RoundUtilsService {


  public int getAssholeLadderNumber(RoundEntity currentRound) {
    return currentRound.getAssholeLadderNumber();
  }

  public int getAssholesNeededForReset(RoundEntity currentRound) {
    int max = currentRound.getAssholeLadderNumber();
    int min = currentRound.getBaseAssholeLadder() / 2;

    return min + Math.round((max - min) * currentRound.getPercentageOfAdditionalAssholes());
  }
}
