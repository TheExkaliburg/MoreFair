package de.kaliburg.morefair.game.ladder.services;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import java.util.List;
import java.util.Optional;

public interface LadderService {

  Optional<LadderEntity> findCurrentLadderWithNumber(int ladderNumber);

  List<LadderEntity> findAllByRound(RoundEntity round);

  Optional<LadderEntity> findLadderById(long ladderId);

  void reloadLadders();

  LadderEntity save(LadderEntity ladder);

  void prepareEarlyAprilFoolsEnding(LadderEntity ladder);
}
