package de.kaliburg.morefair.game.round;

import de.kaliburg.morefair.game.GameEntity;
import de.kaliburg.morefair.game.round.ladder.LadderEntity;
import de.kaliburg.morefair.game.round.ladder.LadderService;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * The RoundService that setups and manages the RoundEntities contained in the GameEntity.
 */
@Service
@Log4j2
public class RoundService {

  private final LadderService ladderService;

  private final List<LadderEntity> ladders = new ArrayList<>();


  public RoundService(LadderService ladderService) {
    this.ladderService = ladderService;
  }

  public RoundEntity createNewRound(GameEntity parent) {
    RoundEntity result = new RoundEntity();

    LadderEntity ladder = ladderService.createNewLadder(result, 1);

    return result;
  }
}
