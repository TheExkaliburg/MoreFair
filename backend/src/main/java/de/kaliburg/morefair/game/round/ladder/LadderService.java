package de.kaliburg.morefair.game.round.ladder;

import de.kaliburg.morefair.game.round.RoundEntity;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class LadderService {

  public LadderEntity createNewLadder(RoundEntity parent, Integer ladderNumber) {
    LadderEntity result = new LadderEntity(UUID.randomUUID(), ladderNumber);

    return result;
  }
}
