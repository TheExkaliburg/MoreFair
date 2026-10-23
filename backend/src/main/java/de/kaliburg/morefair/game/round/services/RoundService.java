package de.kaliburg.morefair.game.round.services;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.api.RoundController;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.core.concurrency.CriticalRegion;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.RoundEventTypes;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.RoundTypeSetBuilder;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import de.kaliburg.morefair.game.round.services.repositories.RoundRepository;
import de.kaliburg.morefair.game.round.services.utils.RoundUtilsService;
import de.kaliburg.morefair.game.season.model.SeasonEntity;
import de.kaliburg.morefair.game.season.services.SeasonService;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The RoundService that setups and manages the RoundEntities contained in the GameEntity.
 */
@Service
@Slf4j
public class RoundService {

  private static final Random random = new Random();
  @Getter
  private final CriticalRegion semaphore = new CriticalRegion(1);
  private final RoundRepository roundRepository;
  private final SeasonService seasonService;
  private final WsUtils wsUtils;
  private final RoundUtilsService roundUtilsService;
  private final FairConfig fairConfig;
  private final RoundInitializationService roundInitializationService;
  private final LoadingCache<Long, RoundEntity> roundCache;
  private Long currentRoundId;

  public RoundService(RoundRepository roundRepository, SeasonService seasonService,
      WsUtils wsUtils,
      RoundUtilsService roundUtilsService, FairConfig fairConfig,
      RoundInitializationService roundInitializationService) {
    this.roundRepository = roundRepository;
    this.seasonService = seasonService;
    this.wsUtils = wsUtils;
    this.roundUtilsService = roundUtilsService;
    this.fairConfig = fairConfig;
    this.roundInitializationService = roundInitializationService;

    this.roundCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.of(30, ChronoUnit.MINUTES))
        .build(id -> this.roundRepository.findById(id).orElse(null));
  }


  public RoundEntity getCurrentRound() {
    try (var ignored = semaphore.enter()) {
      if (currentRoundId == null) {
        SeasonEntity currentSeason = seasonService.getCurrentSeason();
        // Find the newest Round of that Season
        RoundEntity round = roundRepository.findNewestRoundOfSeason(currentSeason.getId())
            .orElse(null);

        // if null -> create first Round of that Season
        // if closed -> create next Round of that Season
        if (round == null || round.isClosed()) {
          round = create(round).orElseThrow();
        } else {
          roundInitializationService.backfillLadders(round);
        }
        currentRoundId = round.getId();
      }
      return roundCache.get(currentRoundId);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }


  public Optional<RoundEntity> findById(long id) {
    try (var ignored = semaphore.enter()) {
      return Optional.of(roundCache.get(id));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }


  public Optional<RoundEntity> findBySeasonAndNumber(SeasonEntity season, int number) {
    RoundEntity currentRound = roundCache.get(currentRoundId);

    if (currentRound == null || number != currentRound.getNumber()
        || season.getId() != currentRound.getSeasonId()) {
      return roundRepository.findBySeasonAndNumber(season.getId(), number);
    } else {
      return Optional.of(currentRound);
    }
  }


  public void closeCurrentRound() {
    try (var ignored = semaphore.enter()) {
      if (currentRoundId == null) {
        return;
      }

      RoundEntity oldRound = roundCache.get(currentRoundId);
      oldRound.setClosedOn(OffsetDateTime.now(ZoneOffset.UTC));
      roundRepository.save(oldRound);
      roundCache.put(oldRound.getId(), oldRound);

      currentRoundId = null;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }


  public RoundEntity updateCurrentRoundTypes(Set<RoundType> newTypes) {
    try (var ignored = semaphore.enter()) {
      RoundEntity currentRound = roundCache.get(currentRoundId);
      currentRound.setTypes(newTypes);

      roundRepository.save(currentRound);
      roundCache.put(currentRound.getId(), currentRound);

      Event<RoundEventTypes> e = new Event<>(RoundEventTypes.UPDATE_TYPES);
      e.setData(currentRound.getTypes());
      wsUtils.convertAndSendToTopic(RoundController.TOPIC_EVENTS_DESTINATION, e);

      return currentRound;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  @Transactional
  public RoundEntity save(RoundEntity round) {
    RoundEntity result = roundRepository.save(round);
    roundCache.put(result.getId(), result);

    return result;
  }


  private Optional<RoundEntity> create(@Nullable RoundEntity previousRound) {
    SeasonEntity currentSeason = seasonService.getCurrentSeason();
    SeasonEntity newestSeason = seasonService.findNewestSeason();

    if (currentSeason.getId() != newestSeason.getId()) {
      // There is not a previousRound if it's a new Season
      previousRound = null;
      currentSeason = seasonService.getCurrentSeason();
    }

    int newRoundNumber = previousRound != null ? previousRound.getNumber() + 1 : 1;

    // If already exists, can't create
    if (roundRepository.findBySeasonAndNumber(currentSeason.getId(), newRoundNumber).isPresent()) {
      return Optional.empty();
    }

    // Determine the new RoundTypes
    RoundTypeSetBuilder builder = new RoundTypeSetBuilder();
    builder.setRoundNumber(newRoundNumber);
    if (previousRound != null) {
      builder.setPreviousRoundType(previousRound.getTypes());
    }
    var types = builder.build();

    // Determine base Points
    double percentage = roundUtilsService.getRoundBasePointRequirementMultiplier(types);
    BigDecimal baseDec = new BigDecimal(fairConfig.getBasePointsToPromote());
    baseDec = baseDec.multiply(BigDecimal.valueOf(percentage));

    // Determining the Asshole Ladder
    int assholeLadder = roundUtilsService.determineAssholeLadder(newRoundNumber, types);

    RoundEntity result = RoundEntity.builder()
        .number(newRoundNumber)
        .assholeLadderNumber(assholeLadder)
        .seasonId(currentSeason.getId())
        .basePointsRequirement(baseDec.toBigInteger())
        .percentageOfAdditionalAssholes(random.nextFloat(100))
        .types(types)
        .build();

    return Optional.of(roundInitializationService.create(result));
  }
}
