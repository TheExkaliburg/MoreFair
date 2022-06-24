package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.game.round.LadderEntity;
import de.kaliburg.morefair.game.round.RankerEntity;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class LadderViewDto {

  private List<RankerDto> rankers = new ArrayList<>();
  private LadderDTO ladder;

  public LadderViewDto(LadderEntity ladder, AccountEntity account) {
    for (RankerEntity ranker : ladder.getRankers()) {
      RankerDto rankerDto = new RankerDto(ranker);
      if (ranker.getAccount().getUuid().equals(account.getUuid())) {
        rankerDto = new RankerPrivateDto(ranker);
        rankerDto.setYou(true);
      }
      this.rankers.add(rankerDto);
    }
    this.ladder = new LadderDTO(ladder);
  }
}
