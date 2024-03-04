package de.kaliburg.morefair.game.ladder.services;

import de.kaliburg.morefair.core.concurrency.CriticalRegion;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import java.util.List;
import java.util.Optional;

public interface LadderService {


  CriticalRegion getSemaphore();

  Optional<LadderEntity> findCurrentLadderWithNumber(int ladderNumber);

  List<LadderEntity> findAllByRound(RoundEntity round);

  LadderEntity createCurrentLadder(int ladderNumber);

  Optional<LadderEntity> findCurrentLadderById(long ladderId);
}
