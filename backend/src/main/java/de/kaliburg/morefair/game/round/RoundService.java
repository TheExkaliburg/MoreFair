package de.kaliburg.morefair.game.round;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.api.GameController;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.EventType;
import de.kaliburg.morefair.game.round.dto.RoundResultsDto;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The RoundService that setups and manages the RoundEntities contained in the GameEntity.
 */
@Service
@Log4j2
public class RoundService {

  private final RoundRepository roundRepository;
  private final LadderService ladderService;
  private final WsUtils wsUtils;
  private final RoundUtils roundUtils;
  private final FairConfig config;
  private final Cache<Integer, RoundResultsDto> roundResultsCache =
      Caffeine.newBuilder().expireAfterAccess(1,
          TimeUnit.HOURS).maximumSize(10).build();


  public RoundService(RoundRepository roundRepository, LadderService ladderService,
      @Lazy WsUtils wsUtils, RoundUtils roundUtils, FairConfig config) {
    this.roundRepository = roundRepository;
    this.ladderService = ladderService;
    this.wsUtils = wsUtils;
    this.roundUtils = roundUtils;
    this.config = config;
  }

  @Transactional
  public void saveStateToDatabase() {
    try {
      ladderService.getLadderSemaphore().acquire();
      try {
        RoundEntity roundEntity = save(getCurrentRound());
        ladderService.saveStateToDatabase(roundEntity);
      } finally {
        ladderService.getLadderSemaphore().release();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a new RoundEntity for the parent GameEntity, filled with an initial LadderEntity and
   * saves it.
   *
   * @return the newly created and saved RoundEntity with 1 Ladder
   */
  @Transactional
  public RoundEntity create(Integer number) {
    RoundEntity previousRound = find(number - 1);
    RoundEntity result = new RoundEntity(number, config, previousRound);
    result = roundRepository.save(result);
    LadderEntity ladder = ladderService.createLadder(result, 1);
    result.getLadders().add(ladder);
    return result;
  }

  @Transactional
  public List<RoundEntity> save(List<RoundEntity> rounds) {
    return roundRepository.saveAll(rounds);
  }

  @Transactional
  public RoundEntity save(RoundEntity round) {
    return roundRepository.save(round);
  }

  /**
   * Overwrites the existing cached rounds with the current one round.
   *
   * @param round the current round
   */
  public void loadIntoCache(RoundEntity round) {
    ladderService.loadIntoCache(round);
  }

  public RoundEntity getCurrentRound() {
    return ladderService.getCurrentRound();
  }

  public RoundEntity find(Integer number) {
    if (getCurrentRound() != null && getCurrentRound().getNumber().equals(number)) {
      return getCurrentRound();
    }
    return roundRepository.findByNumber(number).orElse(null);
  }

  /**
   * Create a new Ranker and updates the highestAssholeCount if necessary.
   *
   * @param account the account that the ranker is part of
   * @return the ranker
   */
  public RankerEntity createNewRanker(AccountEntity account) {
    RankerEntity result = ladderService.createRanker(account);

    Integer assholeCount = result.getAccount().getAssholeCount();
    int baseAssholeLadderNumber = getCurrentRound().getTypes().contains(RoundType.FAST)
        ? getCurrentRound().getBaseAssholeLadder() / 2
        : getCurrentRound().getBaseAssholeLadder();

    if (assholeCount > getCurrentRound().getHighestAssholeCount()
        && !getCurrentRound().getTypes().contains(RoundType.CHAOS)
        && ladderService.find(baseAssholeLadderNumber) == null) {
      getCurrentRound().setHighestAssholeCount(assholeCount);
      ladderService.setCurrentRound(save(getCurrentRound()));
      wsUtils.convertAndSendToTopic(GameController.TOPIC_GLOBAL_EVENTS_DESTINATION, new Event(
          EventType.INCREASE_ASSHOLE_LADDER, account.getId(),
          roundUtils.getAssholeLadderNumber(getCurrentRound())));
    }

    return result;
  }


  public RoundResultsDto getRoundResults(Integer number) {
    RoundResultsDto result = roundResultsCache.getIfPresent(number);
    if (result == null) {
      RoundEntity round = find(number);
      if (round == null) {
        return null;
      }
      result = new RoundResultsDto(round, config);
      roundResultsCache.put(number, result);
    }
    return result;
  }
}
