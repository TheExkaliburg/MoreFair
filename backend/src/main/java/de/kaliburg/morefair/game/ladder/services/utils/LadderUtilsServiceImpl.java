package de.kaliburg.morefair.game.ladder.services.utils;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.round.services.RoundService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LadderUtilsServiceImpl implements LadderUtilsService {

  private final FairConfig config;
  private final RankerService rankerService;
  private final RoundService roundService;

  public Integer getRequiredRankerCountToUnlock(LadderEntity ladder) {
    return config.getMinimumPeopleForPromote();
  }

  public boolean isLadderUnlocked(@NonNull LadderEntity ladder) {
    if (ladder.getTypes().contains(LadderType.END)) {
      return false;
    }
    List<RankerEntity> rankers = rankerService.findAllByLadderId(ladder.getId());
    return rankers.size() >= getRequiredRankerCountToUnlock(ladder);
  }

  public boolean isLadderPromotable(@NonNull LadderEntity ladder) {
    List<RankerEntity> rankers = rankerService.findAllByLadderId(ladder.getId());

    return isLadderUnlocked(ladder)
        && rankers.get(0).getPoints().compareTo(ladder.getBasePointsToPromote()) >= 0;
  }


}
