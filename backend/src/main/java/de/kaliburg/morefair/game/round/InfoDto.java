package de.kaliburg.morefair.game.round;

import de.kaliburg.morefair.FairConfig;
import lombok.Data;

@Data
public class InfoDto {

  private Integer minimumPeopleForPromote;
  private Integer assholeLadder;
  private String baseVinegarNeededToThrow;
  private String baseGrapesNeededToAutoPromote;
  private Integer autoPromoteLadder;
  private Integer manualPromoteWaitTime;
  private RoundType type;

  public InfoDto(RoundEntity currentRound, FairConfig config) {
    minimumPeopleForPromote = config.getBasePeopleToPromote();
    baseVinegarNeededToThrow = config.getBaseVinegarToThrow().toString();
    autoPromoteLadder = config.getAutoPromoteLadder();
    manualPromoteWaitTime = config.getManualPromoteWaitTime();
    baseGrapesNeededToAutoPromote = config.getBaseGrapesToBuyAutoPromote().toString();
    assholeLadder = currentRound.getAssholeLadderNumber();
    type = currentRound.getType();
  }
}
