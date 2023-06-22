package de.kaliburg.morefair.account;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountDetailsDto {

  private UUID uuid;
  private Long accountId;
  private String username;
  private String email;
  private Integer highestCurrentLadder;
  private AccountAccessRole accessRole;

  public AccountDetailsDto(AccountEntity account, Integer highestCurrentLadder) {
    this.uuid = account.getUuid();
    this.accountId = account.getId();
    this.username = account.getDisplayName();
    this.email = account.getUsername();
    this.highestCurrentLadder = highestCurrentLadder;
    this.accessRole = account.getAccessRole();
  }
}
