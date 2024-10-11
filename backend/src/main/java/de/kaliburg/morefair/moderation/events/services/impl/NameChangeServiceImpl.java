package de.kaliburg.morefair.moderation.events.services.impl;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.moderation.events.model.NameChangeEntity;
import de.kaliburg.morefair.moderation.events.services.NameChangeService;
import de.kaliburg.morefair.moderation.events.services.repositories.NameChangeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NameChangeServiceImpl implements NameChangeService {

  private final AccountService accountService;
  private final NameChangeRepository nameChangeRepository;

  @Override
  @Transactional
  public NameChangeEntity updateDisplayName(Long accountId, String displayName) {
    AccountEntity accountEntity = accountService.findById(accountId).orElseThrow();

    NameChangeEntity result = NameChangeEntity.builder()
        .accountId(accountEntity.getId())
        // Showing previous name since new one is in accountEntity (not having data doubled)
        .displayName(accountEntity.getDisplayName())
        .build();

    accountEntity.setDisplayName(displayName);
    accountService.save(accountEntity);

    result = nameChangeRepository.save(result);

    return result;
  }

  @Override
  public List<NameChangeEntity> listAllNameChangesFrom(String displayName) {
    return nameChangeRepository.findWithName(displayName);
  }

  @Override
  public List<NameChangeEntity> listAllNameChangesOf(Long accountId) {
    return nameChangeRepository.findByAccount(accountId);
  }
}
