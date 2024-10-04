package de.kaliburg.morefair.game.round.dto;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.round.RoundEntity;
import de.kaliburg.morefair.game.round.RoundType;
import java.util.Set;
import lombok.Data;

@Data
public class RoundDto {

  private RoundSettingsDto settings;
  private Integer number;
  private Integer assholeLadder;
  private Integer topLadder;
  private Integer autoPromoteLadder;
  private Integer highestAssholeCount;
  private Set<RoundType> types;

  public RoundDto(RoundEntity currentRound, FairConfig config) {
    settings = new RoundSettingsDto(currentRound, config);
    number = currentRound.getNumber();
    autoPromoteLadder = config.getAutoPromoteLadder();
    topLadder = currentRound.getLadders().size();
    assholeLadder = currentRound.getAssholeLadderNumber();
    highestAssholeCount = currentRound.getHighestAssholeCount();
    types = currentRound.getTypes();
  }

  @Data
  public class RoundSettingsDto {

    private String basePointsForPromote;
    private Integer minimumPeopleForPromote;
    private String baseVinegarNeededToThrow;
    private String baseGrapesNeededToAutoPromote;
    private Integer manualPromoteWaitTime;

    public RoundSettingsDto(RoundEntity currentRound, FairConfig config) {
      basePointsForPromote = currentRound.getBasePointsRequirement().toString();
      minimumPeopleForPromote = config.getBaseAssholeLadder();
      baseVinegarNeededToThrow = config.getBaseVinegarToThrow().toString();
      manualPromoteWaitTime = config.getManualPromoteWaitTime();
      baseGrapesNeededToAutoPromote = config.getBaseGrapesToBuyAutoPromote().toString();
    }
  }
}
