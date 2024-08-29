package de.kaliburg.morefair.game.ladder.services;

import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.LadderEventType;

public interface LadderEventService {

  void addEvent(int ladderNumber, Event<LadderEventType> type);

  void handleEvents() throws InterruptedException;
}
