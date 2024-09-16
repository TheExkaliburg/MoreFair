package de.kaliburg.morefair.moderation.events.services;

import de.kaliburg.morefair.moderation.events.model.UserEventEntity;
import de.kaliburg.morefair.moderation.events.model.dto.UserEventRequest;


public interface UserEventService {

  UserEventEntity record(Long accountId, Enum<?> eventType, UserEventRequest request);

}
