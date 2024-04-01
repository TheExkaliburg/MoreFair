package de.kaliburg.morefair.game.round.services.utils.impl;

import de.kaliburg.morefair.game.round.model.UnlocksEntity;
import de.kaliburg.morefair.game.round.services.utils.UnlocksUtilsService;
import org.springframework.stereotype.Service;

@Service
public class UnlocksUtilsServiceImpl implements UnlocksUtilsService {

  @Override
  public int calculateAssholePoints(UnlocksEntity unlocks) {
    int result = 0;
    if (unlocks.getReachedAssholeLadder()) {
      result += 3;
    }
    if (unlocks.getPressedAssholeButton()) {
      result += 7;
    }
    return result;
  }
}
