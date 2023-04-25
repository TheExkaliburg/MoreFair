package de.kaliburg.morefair.game.round.dto;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.round.RoundEntity;
import de.kaliburg.morefair.game.round.RoundType;
import java.util.Set;
import lombok.Data;

@Data
public class RoundDto {

  private RoundSettingsDto settings;
  private Integer assholeLadder;
  private Integer autoPromoteLadder;
  private Set<RoundType> types;

  public RoundDto(RoundEntity currentRound, FairConfig config) {
    settings = new RoundSettingsDto(currentRound, config);
    autoPromoteLadder = config.getAutoPromoteLadder();
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
