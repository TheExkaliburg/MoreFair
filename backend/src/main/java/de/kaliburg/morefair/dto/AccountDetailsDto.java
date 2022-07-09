package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.account.AccountAccessRole;
import de.kaliburg.morefair.account.AccountEntity;
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

  public AccountDetailsDto(AccountEntity account) {
    this.uuid = account.getUuid();
    this.accountId = account.getId();
    // TODO: Actual value
    this.highestCurrentLadder = account.getHighestCurrentLadder();
    this.accessRole = account.getAccessRole();
  }
}
