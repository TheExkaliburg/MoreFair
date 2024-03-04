package de.kaliburg.morefair.data;

import de.kaliburg.morefair.account.model.types.AccountAccessType;
import lombok.Data;
import lombok.NonNull;

@Data
public class ModerationInfoData {

  @NonNull
  private Integer highestLadder;
  @NonNull
  private AccountAccessType yourAccessRole;
}
