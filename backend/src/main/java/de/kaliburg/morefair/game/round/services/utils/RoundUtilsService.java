package de.kaliburg.morefair.game.round.services.utils;

import de.kaliburg.morefair.game.round.model.RoundEntity;

public interface RoundUtilsService {

  int getAssholeLadderNumber(RoundEntity round);

  int getAssholesNeededForReset(RoundEntity round);
}
