package de.kaliburg.morefair.moderation.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserSearchListResponse {

  private List<UserSearchResponse> users;

  @Data
  @Builder
  @AllArgsConstructor
  public static class UserSearchResponse {

    private Long accountId;
    private String displayName;
    private Long lastLogin;

  }
}
