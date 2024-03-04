package de.kaliburg.morefair.game.round.services.mapper;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.dto.RoundDto;
import de.kaliburg.morefair.game.round.model.dto.RoundDto.RoundSettingsDto;
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

    return RoundDto.builder()
        .autoPromoteLadder(fairConfig.getAutoPromoteLadder())
        .topLadder(ladders.size())
        .assholeLadder(round.getAssholeLadderNumber())
        .types(round.getTypes())
        .settings(
            RoundSettingsDto.builder()
                .basePointsForPromote(round.getBasePointsRequirement().toString())
                .minimumPeopleForPromote(fairConfig.getBaseAssholeLadder())
                .baseVinegarNeededToThrow(fairConfig.getBaseVinegarToThrow().toString())
                .manualPromoteWaitTime(fairConfig.getManualPromoteWaitTime())
                .baseGrapesNeededToAutoPromote(
                    fairConfig.getBaseGrapesToBuyAutoPromote().toString()
                )
                .build()
        )
        .build();
  }
}
