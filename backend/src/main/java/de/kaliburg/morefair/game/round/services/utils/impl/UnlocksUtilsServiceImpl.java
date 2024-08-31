package de.kaliburg.morefair.game.round.services.utils.impl;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.UnlocksEntity;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.game.round.services.utils.UnlocksUtilsService;
import de.kaliburg.morefair.game.vinegar.model.VinegarThrowEntity;
import de.kaliburg.morefair.game.vinegar.services.VinegarThrowService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnlocksUtilsServiceImpl implements UnlocksUtilsService {

  private final VinegarThrowService vinegarThrowService;
  private final LadderService ladderService;
  private final RoundService roundService;

  @Override
  public int calculateAssholePoints(UnlocksEntity unlocks) {
    Long accountId = unlocks.getAccountId();

    int result = 0;
    if (unlocks.getReachedAssholeLadder()) {
      result += 2;
    }
    if (unlocks.getPressedAssholeButton()) {
      result += 8;
    }

    RoundEntity currentRound = roundService.getCurrentRound();
    Long assholeLadderId = ladderService
        .findCurrentLadderWithNumber(currentRound.getAssholeLadderNumber())
        .map(LadderEntity::getId)
        .orElse(null);

    List<VinegarThrowEntity> vinThrows = vinegarThrowService
        .findVinegarThrowsOfCurrentRound(accountId).stream()
        .filter(vt -> vt.getThrowerAccountId().equals(accountId))
        .toList();

    long successNonAssholeThrows = vinThrows.stream()
        .filter(t -> !t.getLadderId().equals(assholeLadderId))
        .filter(VinegarThrowEntity::isSuccessful)
        .count();

    if (successNonAssholeThrows > 0) {
      result += 1;
    }

    long assholeThrows = vinThrows.stream()
        .filter(t -> t.getLadderId().equals(assholeLadderId))
        .count();

    if (assholeThrows > 0) {
      result += 1;
    }

    long successAssholeThrows = vinThrows.stream()
        .filter(t -> t.getLadderId().equals(assholeLadderId))
        .filter(VinegarThrowEntity::isSuccessful)
        .count();

    if (successAssholeThrows > 0) {
      result += 1;
    }

    return result;
  }
}
