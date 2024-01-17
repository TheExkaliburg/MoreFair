package de.kaliburg.morefair.game.round.model.dto;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.RoundType;
import java.util.Set;
import lombok.Data;

@Data
public class RoundDto {

  private RoundSettingsDto settings;
  private Integer assholeLadder;
  private Integer topLadder;
  private Integer autoPromoteLadder;
  private Set<RoundType> types;

  public RoundDto(RoundEntity currentRound, FairConfig config) {
    settings = new RoundSettingsDto(currentRound, config);
    autoPromoteLadder = config.getAutoPromoteLadder();
    topLadder = currentRound.getLadders().size();
    assholeLadder = currentRound.getAssholeLadderNumber();
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
