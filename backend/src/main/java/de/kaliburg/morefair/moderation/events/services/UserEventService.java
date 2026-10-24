package de.kaliburg.morefair.moderation.events.services;

import de.kaliburg.morefair.moderation.events.model.UserEventEntity;
import de.kaliburg.morefair.moderation.events.model.UserEventMetadata;


public interface UserEventService {

  UserEventEntity record(Long accountId, Enum<?> eventType, UserEventMetadata metadata);

}
