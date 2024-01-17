package de.kaliburg.morefair.game.ladder.model.dto;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.model.dto.RankerDto;
import de.kaliburg.morefair.game.ranker.model.dto.RankerPrivateDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class LadderDto {

  private List<RankerDto> rankers = new ArrayList<>();
  private Integer number;
  private Integer scaling;
  private Set<LadderType> types;
  private String basePointsToPromote;

  public LadderDto(LadderEntity ladder, AccountEntity account, FairConfig config) {
    basePointsToPromote = ladder.getBasePointsToPromote().toString();
    number = ladder.getNumber();
    scaling = ladder.getScaling();
    types = ladder.getTypes();
    for (RankerEntity ranker : ladder.getRankers()) {
      RankerDto rankerDto = new RankerDto(ranker, config);
      if (ranker.getAccountId().getUuid().equals(account.getUuid())) {
        rankerDto = new RankerPrivateDto(ranker, config);
      }
      this.rankers.add(rankerDto);
    }
  }
}
