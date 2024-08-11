package de.kaliburg.morefair.account.model.dto;

import de.kaliburg.morefair.account.model.types.AccountAccessType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserListResponse {

  private List<User> users;

  @Data
  @Builder
  @AllArgsConstructor
  public static class User {

    private Long accountId;
    private String displayName;
    private String tag;
    private AccountAccessType accessRole;
  }


}
