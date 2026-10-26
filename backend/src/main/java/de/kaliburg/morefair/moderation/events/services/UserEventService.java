package de.kaliburg.morefair.moderation.events.services;

import de.kaliburg.morefair.moderation.events.model.UserEventEntity;
import de.kaliburg.morefair.moderation.events.model.UserEventMetadata;


public interface UserEventService {

  UserEventEntity record(Long accountId, Enum<?> eventType, UserEventMetadata metadata);

  /**
   * Captures the game state snapshot on the calling thread, so it reflects the moment of the call,
   * and persists the event on a background thread to keep the database write out of the game tick.
   */
  void recordAsync(Long accountId, Enum<?> eventType, UserEventMetadata metadata);

}
