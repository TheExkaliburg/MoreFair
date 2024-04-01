package de.kaliburg.morefair.game.round.services.utils;

import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import java.util.Set;

public interface RoundUtilsService {

  int getAssholeLadderNumber(RoundEntity round);

  double getRoundBasePointRequirementMultiplier(Set<RoundType> types);

  int determineAssholeLadder(int roundNumber, Set<RoundType> types);
}
