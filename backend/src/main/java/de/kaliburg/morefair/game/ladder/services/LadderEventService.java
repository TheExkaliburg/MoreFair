package de.kaliburg.morefair.game.ladder.services;

import de.kaliburg.morefair.core.concurrency.CriticalRegion;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.LadderEventTypes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LadderEventService {

  private final CriticalRegion semaphore = new CriticalRegion(1);

  private final Map<Integer, List<Event<LadderEventTypes>>> eventMap = new HashMap<>();

  /**
   * Adds an event to the list of events inside the eventMap. This calls a semaphore and should
   * thereby only be done by the Controllers/API.
   *
   * @param event the event that gets added to the eventMap
   */
  public void addEvent(int ladderNumber, Event<LadderEventTypes> event) {
    try (var ignored = semaphore.enter()) {
      eventMap.get(ladderNumber).add(event);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

}
