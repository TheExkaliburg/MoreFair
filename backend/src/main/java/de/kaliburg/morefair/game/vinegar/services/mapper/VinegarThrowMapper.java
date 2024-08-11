package de.kaliburg.morefair.game.vinegar.services.mapper;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.game.vinegar.model.VinegarThrowEntity;
import de.kaliburg.morefair.game.vinegar.model.dto.VinegarThrowRecordResponse;
import de.kaliburg.morefair.game.vinegar.model.dto.VinegarThrowRecordResponse.ThrowRecord;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VinegarThrowMapper {

  private final LadderService ladderService;
  private final RoundService roundService;

  public VinegarThrowRecordResponse mapVinegarThrowListToVinegarThrowRecords(
      List<VinegarThrowEntity> vinegarThrowList, Long accountId) {

    return VinegarThrowRecordResponse.builder()
        .throwRecords(
            vinegarThrowList.stream().map(this::mapVinegarThrowToThrowRecord).toList()
        )
        .build();

  }


  public VinegarThrowRecordResponse.ThrowRecord mapVinegarThrowToThrowRecord(
      VinegarThrowEntity vinegarThrow) {
    LadderEntity ladder = ladderService.findLadderById(vinegarThrow.getLadderId())
        .orElseThrow();

    RoundEntity round = roundService.findById(ladder.getRoundId()).orElseThrow();

    return ThrowRecord.builder()
        .accountId(vinegarThrow.getThrowerAccountId())
        .targetId(vinegarThrow.getTargetAccountId())
        .timestamp(
            vinegarThrow.getTimestamp().withOffsetSameInstant(ZoneOffset.UTC).toEpochSecond()
        )
        .ladderNumber(ladder.getNumber())
        .roundNumber(round.getNumber())
        .vinegarThrown(vinegarThrow.getVinegarThrown().toString())
        .percentage(vinegarThrow.getPercentageThrown())
        .successType(vinegarThrow.getSuccessType())
        .build();

  }

}
