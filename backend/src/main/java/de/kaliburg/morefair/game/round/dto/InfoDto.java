package de.kaliburg.morefair.game.round.dto;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.round.RoundEntity;
import de.kaliburg.morefair.game.round.RoundType;
import java.util.Set;
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
  private Set<RoundType> types;

  public InfoDto(RoundEntity currentRound, FairConfig config) {
    pointsForPromote = currentRound.getBasePointsRequirement().toString();
    minimumPeopleForPromote = config.getBaseAssholeLadder();
    baseVinegarNeededToThrow = config.getBaseVinegarToThrow().toString();
    autoPromoteLadder = config.getAutoPromoteLadder();
    manualPromoteWaitTime = config.getManualPromoteWaitTime();
    baseGrapesNeededToAutoPromote = config.getBaseGrapesToBuyAutoPromote().toString();
    assholeLadder = currentRound.getAssholeLadderNumber();
    types = currentRound.getTypes();
  }
}
