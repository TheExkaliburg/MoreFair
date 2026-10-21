package de.kaliburg.morefair.game.ladder.services.utils;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import de.kaliburg.morefair.game.round.services.RoundService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LadderUtilsServiceImpl implements LadderUtilsService {

  private final FairConfig config;
  private final RankerService rankerService;
  private final RoundService roundService;

  public Integer getRequiredRankerCountToUnlock(LadderEntity ladder) {
    RoundEntity round = roundService.findById(ladder.getRoundId())
        .orElseThrow();

    if (round.getTypes().contains(RoundType.SPECIAL_100)) {
      return config.getMinimumPeopleForPromote();
    }

    if (round.getTypes().contains(RoundType.APRIL_FOOLS)) {
      return 4;
    }

    if (round.getTypes().contains(RoundType.RACE)) {
      return 1;
    }

    if (round.getTypes().contains(RoundType.FAST)) {
      return config.getMinimumPeopleForPromote();
    }

    return Math.min(Math.max(config.getMinimumPeopleForPromote(), ladder.getScaling()),
        config.getMaximumPeopleForPromote());
  }

  public boolean isLadderUnlocked(@NonNull LadderEntity ladder) {
    if (ladder.hasType(LadderType.END)) {
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

  @Override
  public Integer getBottomGrapes(@NotNull LadderEntity ladder) {
    RoundEntity round = roundService.findById(ladder.getRoundId())
        .orElseThrow();

    if (ladder.getTypes().contains(LadderType.BOUNTIFUL)) {
      return 5;
    } else if (ladder.getTypes().contains(LadderType.LAVA)) {
      if (round.getTypes().contains(RoundType.APRIL_FOOLS)) {
        return -1;
      }
      return 0;
    }
    return 1;
  }
}
