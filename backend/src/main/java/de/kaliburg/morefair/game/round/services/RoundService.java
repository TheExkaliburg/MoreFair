package de.kaliburg.morefair.game.round.services;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.season.model.SeasonEntity;
import java.util.Optional;

public interface RoundService {

  RoundEntity getCurrentRound();

  Optional<RoundEntity> findBySeasonAndNumber(SeasonEntity currentSeason, int number);

  void updateHighestAssholeCountOfCurrentRound(AccountEntity account);
}
