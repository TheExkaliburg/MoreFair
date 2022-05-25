package de.kaliburg.morefair.game.ladder;

import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.game.ranker.RankerService;
import de.kaliburg.morefair.game.round.RoundEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * The LadderService that setups and manages the LadderEntities contained in a RoundEntity.
 */
@Service
@Log4j2
public class LadderService {

  private final RankerService rankerService;
  private final LadderRepository ladderRepository;
  private final Semaphore ladderSemaphore = new Semaphore(1);
  private final Semaphore eventSemaphore = new Semaphore(1);
  private Map<Integer, LadderEntity> currentLadderMap = new HashMap<>();
  private Map<Integer, List<Event>> eventMap = new HashMap<>();

  public LadderService(RankerService rankerService, LadderRepository ladderRepository) {
    this.rankerService = rankerService;
    this.ladderRepository = ladderRepository;
  }

  /**
   * Creates a new Ladder for the round of its parent with the specific ladderNumber and saves it.
   * <p>
   * If the round is the current Round the ladder will be added to the cache
   * </p>
   *
   * @param parent       the parent RoundEntity, that this ladder is part of
   * @param ladderNumber the number of the LadderEntity in that round
   * @return the newly created and saved LadderEntity
   */
  public LadderEntity createLadder(RoundEntity parent, Integer ladderNumber) {
    LadderEntity result = ladderRepository.save(new LadderEntity(ladderNumber, parent));

    if (result.getRound().isCurrentRound()) {
      currentLadderMap.put(ladderNumber, result);
    }

    return result;
  }

  /**
   * Updates an existing LadderEntity and saves it.
   *
   * @param ladder the ladder that needs to be updated
   * @return the updated and saved ladder
   */
  public LadderEntity updateLadder(LadderEntity ladder) {
    return ladderRepository.save(ladder);
  }

  /**
   * Overwrites the existing cached ladders with the ladders from this round.
   *
   * @param round the round that get their ladders loaded into cache
   */
  public void loadIntoCache(RoundEntity round) {
    currentLadderMap = new HashMap<>();
    round.getLadders().forEach(ladder -> {
      currentLadderMap.put(ladder.getNumber(), ladder);
      eventMap.put(ladder.getNumber(), new ArrayList<>());
    });
  }

  /**
   * Adds an event to the list of events inside the eventMap. This calls a semaphore and should
   * thereby only be done by the Controllers/API.
   *
   * @param ladderNumber the key for the eventMap, that identifies which ladder is targeted by the
   *                     event
   * @param event        the event that gets added to the eventMap
   */
  public void addEvent(Integer ladderNumber, Event event) {
    try {
      eventSemaphore.acquire();
      try {
        eventMap.get(ladderNumber).add(event);
      } finally {
        eventSemaphore.release();
      }
    } catch (InterruptedException e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }
}
