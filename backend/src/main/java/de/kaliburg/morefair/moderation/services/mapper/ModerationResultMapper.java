package de.kaliburg.morefair.moderation.services.mapper;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.moderation.model.UserSearchListResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModerationResultMapper {


  public UserSearchListResponse mapToUserSearchList(List<AccountEntity> accounts) {
    List<UserSearchListResponse.UserSearchResponse> users = accounts.stream()
        .map(this::mapToUser)
        .toList();

    return UserSearchListResponse.builder()
        .users(users)
        .build();
  }

  public UserSearchListResponse.UserSearchResponse mapToUser(AccountEntity user) {
    return UserSearchListResponse.UserSearchResponse.builder()
        .accountId(user.getId())
        .displayName(user.getDisplayName())
        .lastLogin(user.getLastLogin().toEpochSecond())
        .build();
  }
}
