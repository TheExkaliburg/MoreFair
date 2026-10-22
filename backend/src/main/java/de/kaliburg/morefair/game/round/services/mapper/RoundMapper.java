package de.kaliburg.morefair.game.round.services.mapper;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.dto.RoundDto;
import de.kaliburg.morefair.game.round.model.dto.RoundDto.RoundSettingsDto;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The Mapper that can convert the {@link RoundEntity RoundEntities} to DTOs.
 */
@Component
@RequiredArgsConstructor
public class RoundMapper {

  private final FairConfig fairConfig;

  private final LadderService ladderService;


  /**
   * Mapping a {@link RoundEntity} to a {@link RoundDto}.
   *
   * @param round the {@link RoundEntity}
   * @return the {@link RoundDto}
   */
  public RoundDto mapToRoundDto(RoundEntity round) {
    List<LadderEntity> ladders = ladderService.findAllByRound(round);
    int topLadder = ladders.stream()
        .filter(LadderEntity::isReached)
        .mapToInt(LadderEntity::getNumber)
        .max()
        .orElse(1);

    int assholeNumber = round.getAssholeLadderNumber();
    if (round.getTypes().contains(RoundType.APRIL_FOOLS)) {
      assholeNumber = 100;
    }

    return RoundDto.builder()
        .autoPromoteLadder(fairConfig.getAutoPromoteLadder())
        .topLadder(topLadder)
        .assholeLadder(assholeNumber)
        .types(round.getTypes())
        .settings(
            RoundSettingsDto.builder()
                .basePointsForPromote(round.getBasePointsRequirement().toString())
                .minimumPeopleForPromote(fairConfig.getMinimumPeopleForPromote())
                .maximumPeopleForPromote(fairConfig.getMaximumPeopleForPromote())
                .baseVinegarNeededToThrow(fairConfig.getBaseVinegarToThrow().toString())
                .manualPromoteWaitTime(fairConfig.getManualPromoteWaitTime())
                .baseGrapesNeededToAutoPromote(
                    fairConfig.getBaseGrapesToBuyAutoPromote().toString()
                )
                .minVinegarThrown(fairConfig.getMinVinegarPercentageThrown())
                .maxVinegarThrown(fairConfig.getMaxVinegarThrown())
                .build()
        )
        .number(round.getNumber())
        .build();
  }
}
