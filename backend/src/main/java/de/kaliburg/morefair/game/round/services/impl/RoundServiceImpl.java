package de.kaliburg.morefair.game.round.services.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.core.concurrency.CriticalRegion;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.RoundTypeSetBuilder;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.game.round.services.repositories.RoundRepository;
import de.kaliburg.morefair.game.round.services.utils.impl.RoundUtilsServiceImpl;
import de.kaliburg.morefair.game.season.model.SeasonEntity;
import de.kaliburg.morefair.game.season.services.AchievementsService;
import de.kaliburg.morefair.game.season.services.SeasonService;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The RoundService that setups and manages the RoundEntities contained in the GameEntity.
 */
@Service
@Slf4j
public class RoundServiceImpl implements RoundService {

  private static final Random random = new Random();
  private final CriticalRegion semaphore = new CriticalRegion(1);
  private final RoundRepository roundRepository;
  private final SeasonService seasonService;
  private final AchievementsService achievementsService;
  private final WsUtils wsUtils;
  private final RoundUtilsServiceImpl roundUtilsService;
  private final FairConfig fairConfig;
  private final LoadingCache<Long, RoundEntity> roundCache;
  private Long currentRoundId;

  public RoundServiceImpl(RoundRepository roundRepository, SeasonService seasonService,
      AchievementsService achievementsService, WsUtils wsUtils,
      RoundUtilsServiceImpl roundUtilsService, FairConfig fairConfig) {
    this.roundRepository = roundRepository;
    this.seasonService = seasonService;
    this.achievementsService = achievementsService;
    this.wsUtils = wsUtils;
    this.roundUtilsService = roundUtilsService;
    this.fairConfig = fairConfig;

    this.roundCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.of(30, ChronoUnit.MINUTES))
        .build(id -> this.roundRepository.findById(id).orElse(null));
  }

  @Override
  @Transactional
  public RoundEntity getCurrentRound() {
    try (var ignored = semaphore.enter()) {
      if (currentRoundId == null) {
        SeasonEntity currentSeason = seasonService.getCurrentSeason();
        // Find newest Round of that Season
        RoundEntity round = roundRepository.findNewestRoundOfSeason(currentSeason.getId())
            .orElse(null);

        // if null -> create first Round of that Season
        // if closed -> create next Round of that Season
        if (round == null || round.isClosed()) {
          round = create(round).orElseThrow();
        }
        currentRoundId = round.getId();
      }
      return roundCache.get(currentRoundId);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<RoundEntity> findById(long id) {
    try (var ignored = semaphore.enter()) {
      return Optional.of(roundCache.get(id));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<RoundEntity> findBySeasonAndNumber(SeasonEntity season, int number) {
    RoundEntity currentRound = roundCache.get(currentRoundId);
    if (number != currentRound.getNumber()) {
      return roundRepository.findBySeasonAndNumber(season.getId(), number);
    } else {
      return Optional.of(currentRound);
    }
  }

  @Override
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
      throw new RuntimeException(e);
    }
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

    return Optional.of(roundRepository.save(result));
  }
}
