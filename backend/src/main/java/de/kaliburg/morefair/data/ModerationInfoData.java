package de.kaliburg.morefair.data;

import de.kaliburg.morefair.account.AccountAccessRole;
import lombok.Data;
import lombok.NonNull;

@Data
public class ModerationInfoData {

  @NonNull
  private Integer highestLadder;
  @NonNull
  private AccountAccessRole yourAccessRole;
}
