package de.kaliburg.morefair.game.vinegar.services.impl;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.data.VinegarData.VinegarSuccessType;
import de.kaliburg.morefair.events.types.LadderEventType;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.ranker.services.utils.RankerUtilsService;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.game.season.services.AchievementsService;
import de.kaliburg.morefair.game.season.services.SeasonService;
import de.kaliburg.morefair.game.vinegar.model.VinegarThrowEntity;
import de.kaliburg.morefair.game.vinegar.model.VinegarThrowEntity.VinegarThrowEntityBuilder;
import de.kaliburg.morefair.game.vinegar.services.VinegarThrowService;
import de.kaliburg.morefair.game.vinegar.services.repositories.VinegarThrowRepository;
import de.kaliburg.morefair.statistics.services.StatisticsService;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VinegarThrowServiceImpl implements VinegarThrowService {

  private final SeasonService seasonService;
  private final RoundService roundService;
  private final AchievementsService achievementsService;
  private final VinegarThrowRepository repository;
  private final LadderService ladderService;
  private final AccountService accountService;
  private final RankerService rankerService;
  private final RankerUtilsService rankerUtilsService;
  private final StatisticsService statisticsService;
  private final FairConfig fairConfig;

  @Override
  public Optional<VinegarThrowEntity> throwVinegar(Event<LadderEventType> event) {
    if (!event.getEventType().equals(LadderEventType.THROW_VINEGAR)) {
      throw new IllegalArgumentException(
          "Can only run if the Event Type is THROW_VINEGAR and not " + event.getEventType() + "."
      );
    }

    AccountEntity throwerAccount = accountService.findById(event.getAccountId()).orElseThrow();
    RankerEntity throwerRanker = rankerService.findHighestActiveRankerOfAccount(throwerAccount)
        .orElseThrow();

    LadderEntity ladder = ladderService.findLadderById(throwerRanker.getLadderId()).orElseThrow();

    List<RankerEntity> rankers = rankerService.findAllByLadderId(ladder.getId());
    RankerEntity targetRanker = rankers.get(0);
    AccountEntity targetAccount = accountService.findById(targetRanker.getAccountId())
        .orElseThrow();

    int percentage = (int) event.getData();

    if (targetRanker.isAutoPromote() || ladder.getTypes().contains(LadderType.FREE_AUTO)) {
      log.info("[L{}] {} (#{}) tried to throw Vinegar at {} (#{}), but they had Auto-Promote!",
          ladder.getNumber(), throwerAccount.getDisplayName(), throwerAccount.getId(),
          targetAccount.getDisplayName(), targetAccount.getId());
      return Optional.empty();
    }

    if (rankerUtilsService.canThrowVinegarAt(throwerRanker, targetRanker, percentage)) {
      statisticsService.recordVinegarThrow(
          throwerRanker, targetRanker, ladder, roundService.getCurrentRound()
      );

      BigInteger throwerVinegar = throwerRanker.getVinegar();
      BigInteger thrownVinegar = throwerVinegar
          .multiply(BigInteger.valueOf(percentage))
          .divide(BigInteger.valueOf(100));
      BigInteger targetVinegar = targetRanker.getVinegar();
      BigInteger targetWine = targetRanker.getWine();
      BigInteger restoredVinegar = BigInteger.ZERO;

      log.info("[L{}] {} (#{}) is using their {} ({}%) Vinegar on {} (#{}) with {} Vinegar",
          ladder.getNumber(), throwerAccount.getDisplayName(), throwerAccount.getId(),
          throwerVinegar, percentage,
          targetAccount.getDisplayName(), targetAccount.getId(), targetVinegar);

      Integer currentStreak = getCurrentStealStreak(throwerAccount.getId(), targetAccount.getId());
      VinegarThrowEntityBuilder builder = VinegarThrowEntity.builder()
          .throwerAccountId(throwerAccount.getId())
          .targetAccountId(targetAccount.getId())
          .ladderId(ladder.getId())
          .vinegarThrown(thrownVinegar)
          .percentageThrown(percentage)
          .vinegarDefended(targetVinegar)
          .wineDefended(targetWine)
          .stolenStreak(currentStreak)
          .stolenPoints(0);

      boolean isWineShieldActive = targetRanker.getVinegar().compareTo(targetWine) < 0;
      VinegarSuccessType successType = null;

      BigInteger passedVinegar = thrownVinegar;
      if (isWineShieldActive) {

        if (targetWine.compareTo(thrownVinegar) <= 0) {
          // BROKE THROUGH SHIELD

          passedVinegar = passedVinegar.subtract(targetWine);
          restoredVinegar = restoredVinegar.add(
              throwerVinegar
                  .multiply(BigInteger.valueOf(fairConfig.getMinVinegarPercentageThrown()))
                  .divide(BigInteger.valueOf(200))
          );
        } else {
          // DEFENDED WITH SHIELD
          passedVinegar = BigInteger.ZERO;
        }

        // OneShot either Way
        successType = VinegarSuccessType.SHIELDED;
        targetRanker.setWine(BigInteger.ZERO);
      }

      // Handle the Comparing Logic
      if (targetVinegar.compareTo(passedVinegar) > 0) {
        // DEFENDED

        // Only remove the same fraction of that vinegar
        BigInteger subtractedVinegar = passedVinegar
            .multiply(BigInteger.valueOf(percentage))
            .divide(BigInteger.valueOf(100));

        throwerRanker.setVinegar(throwerVinegar.subtract(thrownVinegar).add(restoredVinegar));
        targetRanker.setVinegar(targetVinegar.subtract(subtractedVinegar));
        successType = Objects.equals(successType, VinegarSuccessType.SHIELDED)
            ? VinegarSuccessType.SHIELD_DEFENDED : VinegarSuccessType.DEFENDED;
      } else {
        // THROW DOWN

        // Getting half of the minimum Vinegar needed to throw back
        restoredVinegar = restoredVinegar.add(
            throwerVinegar
                .multiply(BigInteger.valueOf(fairConfig.getMinVinegarPercentageThrown()))
                .divide(BigInteger.valueOf(200))
        );

        throwerRanker.setVinegar(throwerVinegar.subtract(thrownVinegar).add(restoredVinegar));
        targetRanker.setVinegar(BigInteger.ZERO);
        successType = Objects.equals(successType, VinegarSuccessType.SHIELDED)
            ? VinegarSuccessType.DOUBLE_SUCCESS : VinegarSuccessType.SUCCESS;

        int stolenPoints = 0;
        if (currentStreak < 0) {
          // steal reverse points -> check if canSteal too
          stolenPoints = currentStreak;
          currentStreak = 0;
        }

        if (canSteal(throwerAccount.getId(), targetAccount.getId())) {
          stolenPoints++;
          currentStreak++;
        }

        builder.stolenPoints(stolenPoints);
        builder.stolenStreak(currentStreak);
      }

      VinegarThrowEntity result = builder
          .successType(successType)
          .build();

      var throwerAch = achievementsService.findOrCreateByAccountInCurrentSeason(
          throwerAccount.getId());
      var targetAch = achievementsService.findOrCreateByAccountInCurrentSeason(
          targetAccount.getId());

      result.setStolenPoints(Math.min(targetAch.getAssholePoints(), result.getStolenPoints()));
      throwerAch.setAssholePoints(throwerAch.getAssholePoints() + result.getStolenPoints());
      targetAch.setAssholePoints(targetAch.getAssholePoints() - result.getStolenPoints());

      result = repository.save(result);
      achievementsService.save(throwerAch);
      achievementsService.save(targetAch);

      return Optional.of(result);
    }

    return Optional.empty();
  }

  @Override
  public List<VinegarThrowEntity> findVinegarThrowsOfCurrentSeason(Long accountId) {
    return repository.findAllByAccountId(accountId);
  }

  @Override
  public List<VinegarThrowEntity> findVinegarThrowsOfCurrentRound(Long accountId) {
    RoundEntity currentRound = roundService.getCurrentRound();
    return repository.findAllByAccountIdAndRoundId(accountId, currentRound.getId());
  }

  private boolean canSteal(Long throwerAccountId, Long targetAccountId) {
    // TODO: return if account can steal from the other (based on ah-points)
    // Right now completely disables this mechanic
    return false;
  }

  private Integer getCurrentStealStreak(Long throwerAccountId, Long targetAccountId) {
    // TODO: Implement negative if stolen from (revenge), positive if thrower has stolen already
    // Right now the streak is always 0
    return 0;
  }
}
