package de.kaliburg.morefair.game.round.model.dto;

import de.kaliburg.morefair.game.round.model.RoundType;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
public class RoundDto {

  private RoundSettingsDto settings;
  private Integer assholeLadder;
  private Integer topLadder;
  private Integer autoPromoteLadder;
  private Set<RoundType> types;

  @Data
  @Builder
  @RequiredArgsConstructor
  public static class RoundSettingsDto {

    private String basePointsForPromote;
    private Integer minimumPeopleForPromote;
    private String baseVinegarNeededToThrow;
    private String baseGrapesNeededToAutoPromote;
    private Integer manualPromoteWaitTime;

  }
}
