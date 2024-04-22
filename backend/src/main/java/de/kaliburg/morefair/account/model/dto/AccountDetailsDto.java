package de.kaliburg.morefair.account.model.dto;

import de.kaliburg.morefair.account.model.types.AccountAccessType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AccountDetailsDto {

  private UUID uuid;
  private Long accountId;
  private String username;
  private String email;
  private Integer highestCurrentLadder;
  private AccountAccessType accessRole;
  private AccountSettingsDto settings;

}
