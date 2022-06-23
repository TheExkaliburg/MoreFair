package de.kaliburg.morefair.game.ladder;

import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.data.JoinData;
import de.kaliburg.morefair.events.types.EventType;
import de.kaliburg.morefair.game.ranker.RankerEntity;
import de.kaliburg.morefair.game.ranker.RankerService;
import de.kaliburg.morefair.game.round.RoundEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * The LadderService that setups and manages the LadderEntities contained in a RoundEntity. This
 * Service only handles the matters that regard a specific ladders, like game logic and user input.
 *
 * <p>For global events look at {@link de.kaliburg.morefair.game.round.RoundService} or for
 * chats and message at {@link de.kaliburg.morefair.game.chat.ChatService} or
 * {@link de.kaliburg.morefair.game.chat.message.MessageService}
 */
@Service
@Log4j2
public class LadderService {

  private final RankerService rankerService;
  private final LadderRepository ladderRepository;
  private final Semaphore eventSemaphore = new Semaphore(1);
  private final Semaphore ladderSemaphore = new Semaphore(1);
  @Getter
  private RoundEntity currentRound;
  private Map<Integer, LadderEntity> currentLadderMap = new HashMap<>();
  private Map<Integer, List<Event>> eventMap = new HashMap<>();

  public LadderService(RankerService rankerService, LadderRepository ladderRepository) {
    this.rankerService = rankerService;
    this.ladderRepository = ladderRepository;
  }

  /**
   * Creates a new Ladder for the round of its round with the specific ladderNumber and saves it.
   * <p>
   * If the round is the current Round the ladder will be added to the cache
   * </p>
   *
   * @param round        the round RoundEntity, that this ladder is part of
   * @param ladderNumber the number of the LadderEntity in that round
   * @return the newly created and saved LadderEntity
   */
  public LadderEntity createLadder(RoundEntity round, Integer ladderNumber) {
    LadderEntity result = new LadderEntity(ladderNumber, round);
    result = ladderRepository.save(result);
    if (currentRound.getUuid().equals(round.getUuid())) {
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
    currentRound = round;
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
   * @param event the event that gets added to the eventMap
   */
  public void addEvent(@NonNull Integer ladderNumber, Event event) {
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

  /**
   * Finds a ladder based on the uuid. It searches first inside the cache, but if it doesn't find
   * any, it searches the database. If the round of the ladder from the database is the same as the
   * currentRound, it adds the ladder to the database.
   *
   * @param uuid The uuid of the ladder
   * @return the ladder
   */
  public LadderEntity find(UUID uuid) {
    return currentLadderMap.values().stream()
        .filter(ladder -> ladder.getUuid().equals(uuid)).findFirst()
        .orElseGet(() -> {
          LadderEntity result = ladderRepository.findByUuid(uuid).orElseThrow();
          if (result.getRound().getUuid().equals(currentRound.getUuid())) {
            currentLadderMap.put(result.getNumber(), result);
          }
          return result;
        });
  }

  /**
   * Finds a ladder based on the id. It searches first inside the cache, but if it doesn't find any,
   * it searches the database. If the round of the ladder from the database is the same as the
   * currentRound, it adds the ladder to the database.
   *
   * @param id The id of the ladder
   * @return the ladder
   */
  public LadderEntity find(Long id) {
    return currentLadderMap.values().stream()
        .filter(ladder -> ladder.getId().equals(id)).findFirst()
        .orElseGet(() -> {
          LadderEntity result = ladderRepository.findById(id).orElseThrow();
          if (result.getRound().getUuid().equals(currentRound.getUuid())) {
            currentLadderMap.put(result.getNumber(), result);
          }
          return result;
        });
  }

  /**
   * Finds a ladder based on the round and the ladder number. It searches first inside the cache,
   * but if it doesn't find any, it searches the database. If the round of the ladder from the
   * database is the same as the currentRound, it adds the ladder to the database.
   *
   * @param round  The round, that the ladder is part of
   * @param number the number of the ladder in that round
   * @return the ladder
   */
  public LadderEntity find(RoundEntity round, Integer number) {
    return currentLadderMap.values().stream()
        .filter(ladder -> ladder.getRound().equals(round) && ladder.getNumber().equals(number))
        .findFirst()
        .orElseGet(() -> {
          LadderEntity result = ladderRepository.findByRoundAndNumber(round, number).orElseThrow();
          if (result.getRound().getUuid().equals(currentRound.getUuid())) {
            currentLadderMap.put(result.getNumber(), result);
          }
          return result;
        });
  }

  /**
   * Finds a ladder based of the ladder number. It searches ONLY inside the cache, since it assumes
   * that the ladder is part of the current round (which should be already cached)
   *
   * @param number the number of the ladder
   * @return the ladder
   */
  public LadderEntity find(Integer number) {
    LadderEntity result = currentLadderMap.get(number);
    if (result == null) {
      throw new NoSuchElementException();
    }

    return result;
  }

  /**
   * Creates a new Ranker on ladder 1.
   *
   * <p>Blocks the ladderSemaphore and eventSemaphore for the duration of the time
   *
   * @param account The Account that gets a new Ranker
   * @return The created Ranker
   */
  public RankerEntity createRanker(AccountEntity account) {
    try {
      ladderSemaphore.acquire();
      try {
        eventSemaphore.acquire();
        try {
          return createRanker(account, 1);
        } finally {
          eventSemaphore.release();
        }
      } finally {
        ladderSemaphore.release();
      }
    } catch (InterruptedException e) {
      log.error(e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Creates a new Ranker on a ladder.
   *
   * <p>Does NOT block the ladderSemaphore and eventSemaphore; Therefor it should only be used in
   * an internal function where we already block these semaphores
   *
   * @param account The account that gets a new ranker
   * @param number  The number of the ladder that the ranker gets created on
   * @return The created ranker
   */
  RankerEntity createRanker(AccountEntity account, Integer number) {
    LadderEntity ladder = find(number);

    List<RankerEntity> activeRankersInLadder = account.getActiveRankers().stream()
        .filter(ranker -> ranker.getLadder().getUuid().equals(ladder.getUuid())).toList();

    // Only 1 active ranker per ladder
    if (!activeRankersInLadder.isEmpty()) {
      return activeRankersInLadder.get(0);
    }

    RankerEntity result = rankerService.create(account, ladder, ladder.getRankers().size() + 1);
    ladder.getRankers().add(result);

    Event joinEvent = new Event(EventType.JOIN, account.getId());
    joinEvent.setData(new JoinData(account.getUsername(), account.getTimesAsshole()));
    eventMap.get(number).add(joinEvent);
    return result;
  }
}
