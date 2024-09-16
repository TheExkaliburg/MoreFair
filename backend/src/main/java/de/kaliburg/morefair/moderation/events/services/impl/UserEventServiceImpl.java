package de.kaliburg.morefair.moderation.events.services.impl;

import de.kaliburg.morefair.moderation.events.model.UserEventEntity;
import de.kaliburg.morefair.moderation.events.model.dto.UserEventRequest;
import de.kaliburg.morefair.moderation.events.services.UserEventService;
import de.kaliburg.morefair.moderation.events.services.repositories.UserEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
@Slf4j
public class UserEventServiceImpl implements UserEventService {

  private final UserEventRepository userEventRepository;

  @Override
  @Transactional
  public UserEventEntity record(Long accountId, Enum<?> eventType, UserEventRequest request) {
    log.info("UserEvent (#{}): {} - {}", accountId, eventType.toString(), request);
    UserEventEntity result = UserEventEntity.builder()
        .eventType(eventType.toString())
        .accountId(accountId)
        .isTrusted(request.getIsTrusted())
        .screenX(request.getScreenX())
        .screenY(request.getScreenY())
        .build();

    // TODO: Send to ModPage via WS
    result = userEventRepository.save(result);

    return result;
  }
}
