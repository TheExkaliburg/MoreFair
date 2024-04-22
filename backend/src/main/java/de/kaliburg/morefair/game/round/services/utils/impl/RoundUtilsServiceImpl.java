package de.kaliburg.morefair.game.round.services.utils.impl;

import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import de.kaliburg.morefair.game.round.services.utils.RoundUtilsService;
import java.util.Random;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoundUtilsServiceImpl implements RoundUtilsService {

  private static final Random random = new Random();

  public int getAssholeLadderNumber(RoundEntity currentRound) {
    return currentRound.getAssholeLadderNumber();
  }

  @Override
  public double getRoundBasePointRequirementMultiplier(Set<RoundType> types) {
    double lowerBound = 0.5f;
    double upperBound = 1.5f;

    if (types.contains(RoundType.SPECIAL_100)) {
      return upperBound;
    }

    if (types.contains(RoundType.CHAOS)) {
      lowerBound /= 2.0f;
      upperBound *= 1.25f;
    } else if (types.contains(RoundType.FAST)) {
      lowerBound /= 2.0f;
      upperBound /= 2.0f;
    } else if (types.contains(RoundType.SLOW)) {
      lowerBound *= 1.25f;
      upperBound *= 1.25f;
    }

    return random.nextDouble(lowerBound, upperBound);
  }

  @Override
  public int determineAssholeLadder(int roundNumber, Set<RoundType> types) {
    int baseAsshole;
    int additionalLadders;

    if (types.contains(RoundType.CHAOS)) {
      if (types.contains(RoundType.SHORT)) {
        baseAsshole = 5;
        additionalLadders = random.nextInt(6);
      } else if (types.contains(RoundType.LONG)) {
        baseAsshole = 25;
        additionalLadders = random.nextInt(11);
      } else {
        baseAsshole = 10;
        additionalLadders = random.nextInt(21);
      }
    } else {
      if (types.contains(RoundType.SHORT)) {
        baseAsshole = 10;
      } else if (types.contains(RoundType.LONG)) {
        baseAsshole = 20;
      } else {
        baseAsshole = 15;
      }

      int cycle = roundNumber % 20;
      additionalLadders = cycle > 10 ? 20 - cycle : cycle;
      additionalLadders += random.nextInt(-1, 2);
      additionalLadders = Math.max(0, Math.min(10, additionalLadders));
    }

    return baseAsshole + additionalLadders;
  }
}
