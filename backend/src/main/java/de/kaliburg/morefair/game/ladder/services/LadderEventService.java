package de.kaliburg.morefair.game.ladder.services;

import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.LadderEventType;
import de.kaliburg.morefair.moderation.events.model.UserEventMetadata;

public interface LadderEventService {

  default void addEvent(int ladderNumber, Event<LadderEventType> event) {
    addEvent(ladderNumber, event, null);
  }

  void addEvent(int ladderNumber, Event<LadderEventType> event, UserEventMetadata metadata);

  void handleEvents() throws InterruptedException;
}
