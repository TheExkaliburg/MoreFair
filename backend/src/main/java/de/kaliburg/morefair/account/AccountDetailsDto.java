package de.kaliburg.morefair.account;

import de.kaliburg.morefair.game.round.RoundEntity;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountDetailsDto {

  private UUID uuid;
  private Long accountId;
  private Integer highestCurrentLadder;
  private AccountAccessRole accessRole;

  public AccountDetailsDto(AccountEntity account, RoundEntity currentRound) {
    this.uuid = account.getUuid();
    this.accountId = account.getId();
    this.highestCurrentLadder = account.getHighestCurrentLadder(currentRound);
    this.accessRole = account.getAccessRole();
  }
}
