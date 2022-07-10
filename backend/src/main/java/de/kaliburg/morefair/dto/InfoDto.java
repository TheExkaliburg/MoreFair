package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.round.RoundEntity;
import de.kaliburg.morefair.game.round.RoundType;
import lombok.Data;

@Data
public class InfoDto {

  private String pointsForPromote;
  private Integer minimumPeopleForPromote;
  private Integer assholeLadder;
  private String baseVinegarNeededToThrow;
  private String baseGrapesNeededToAutoPromote;
  private Integer autoPromoteLadder;
  private Integer manualPromoteWaitTime;
  private RoundType roundType;

  public InfoDto(RoundEntity currentRound, FairConfig config) {
    pointsForPromote = config.getBasePointsToPromote().toString();
    minimumPeopleForPromote = config.getBasePeopleToPromote();
    baseVinegarNeededToThrow = config.getBaseVinegarToThrow().toString();
    autoPromoteLadder = config.getAutoPromoteLadder();
    manualPromoteWaitTime = config.getManualPromoteWaitTime();
    baseGrapesNeededToAutoPromote = config.getBaseGrapesToBuyAutoPromote().toString();
    assholeLadder = currentRound.getAssholeLadderNumber();
    roundType = currentRound.getType();
  }
}
