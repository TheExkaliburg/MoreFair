package de.kaliburg.morefair.moderation.events.services.mapper;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.moderation.events.model.NameChangeEntity;
import de.kaliburg.morefair.moderation.events.model.dto.NameChangeListResponse;
import de.kaliburg.morefair.moderation.events.model.dto.NameChangeListResponse.NameChangeResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NameChangeMapper {

  private final AccountService accountService;

  public NameChangeListResponse mapToNameChangeList(List<NameChangeEntity> entities) {
    List<NameChangeResponse> list = entities.stream().map(this::mapToNameChangeResponse).toList();

    return NameChangeListResponse.builder()
        .list(list)
        .build();
  }

  public NameChangeResponse mapToNameChangeResponse(NameChangeEntity entity) {
    AccountEntity account = accountService.findById(entity.getAccountId()).orElseThrow();

    return NameChangeResponse.builder()
        .currentName(account.getDisplayName())
        .displayName(entity.getDisplayName())
        .accountId(account.getId())
        .timestamp(entity.getCreatedOn().toEpochSecond())
        .build();
  }

}
