package de.kaliburg.morefair.statistics.services.impl;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.game.season.model.SeasonEntity;
import de.kaliburg.morefair.game.season.services.SeasonService;
import de.kaliburg.morefair.statistics.records.model.AutoPromoteRecordEntity;
import de.kaliburg.morefair.statistics.records.model.BiasRecordEntity;
import de.kaliburg.morefair.statistics.records.model.LadderRecord;
import de.kaliburg.morefair.statistics.records.model.LoginRecordEntity;
import de.kaliburg.morefair.statistics.records.model.MultiRecordEntity;
import de.kaliburg.morefair.statistics.records.model.PromoteRecordEntity;
import de.kaliburg.morefair.statistics.records.model.RankerRecord;
import de.kaliburg.morefair.statistics.records.model.RoundRecord;
import de.kaliburg.morefair.statistics.records.model.ThrowVinegarRecordEntity;
import de.kaliburg.morefair.statistics.records.services.repositories.AutoPromoteRecordRepository;
import de.kaliburg.morefair.statistics.records.services.repositories.BiasRecordRepository;
import de.kaliburg.morefair.statistics.records.services.repositories.LoginRecordRepository;
import de.kaliburg.morefair.statistics.records.services.repositories.MultiRecordRepository;
import de.kaliburg.morefair.statistics.records.services.repositories.PromoteRecordRepository;
import de.kaliburg.morefair.statistics.records.services.repositories.ThrowVinegarRecordRepository;
import de.kaliburg.morefair.statistics.results.ActivityAnalysisEntity;
import de.kaliburg.morefair.statistics.results.ActivityAnalysisRepository;
import de.kaliburg.morefair.statistics.results.RoundStatisticsEntity;
import de.kaliburg.morefair.statistics.results.RoundStatisticsRepository;
import de.kaliburg.morefair.statistics.services.StatisticsService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class StatisticsServiceImpl implements StatisticsService {

  private final LoginRecordRepository loginRecordRepository;
  private final BiasRecordRepository biasRecordRepository;
  private final MultiRecordRepository multiRecordRepository;
  private final AutoPromoteRecordRepository autoPromoteRecordRepository;
  private final PromoteRecordRepository promoteRecordRepository;
  private final ThrowVinegarRecordRepository throwVinegarRecordRepository;
  private final RoundStatisticsRepository roundStatisticsRepository;
  private final ActivityAnalysisRepository activityAnalysisRepository;


  private final RoundService roundService;
  private final RankerService rankerService;
  private final LadderService ladderService;
  private final SeasonService seasonService;

  public void recordLogin(AccountEntity account) {
    loginRecordRepository.save(new LoginRecordEntity(account));
  }

  /**
   * Records the moment before a user biases.
   *
   * @param ranker the Ranker of the user before they bias
   * @param ladder the ladder with all rankers before they multiply
   * @param round  the round with all ladders before they multiply
   */
  public void recordBias(RankerEntity ranker, LadderEntity ladder, RoundEntity round) {
    RankerRecord rankerRecord = new RankerRecord(ranker, round);
    LadderRecord ladderRecord = new LadderRecord(ladder,
        rankerService.findAllByLadderId(ladder.getId()));

    Map<LadderEntity, List<RankerEntity>> roundState = ladderService.findAllByRound(round).stream()
        .collect(Collectors.toMap(l -> l, l -> rankerService.findAllByLadderId(l.getId())));
    RoundRecord roundRecord = new RoundRecord(round, roundState);
    biasRecordRepository.save(new BiasRecordEntity(rankerRecord, ladderRecord, roundRecord));
  }

  /**
   * Records the moment before a user multiplies.
   *
   * @param ranker the Ranker of the user before they multiply
   * @param ladder the ladder with all rankers before they multiply
   * @param round  the round with all ladders before they multiply
   */
  public void recordMulti(RankerEntity ranker, LadderEntity ladder, RoundEntity round) {
    RankerRecord rankerRecord = new RankerRecord(ranker, round);
    LadderRecord ladderRecord = new LadderRecord(ladder,
        rankerService.findAllByLadderId(ladder.getId()));

    Map<LadderEntity, List<RankerEntity>> roundState = ladderService.findAllByRound(round).stream()
        .collect(Collectors.toMap(l -> l, l -> rankerService.findAllByLadderId(l.getId())));

    RoundRecord roundRecord = new RoundRecord(round, roundState);
    multiRecordRepository.save(new MultiRecordEntity(rankerRecord, ladderRecord, roundRecord));
  }

  /**
   * Records the moment before a user buys auto-promote.
   *
   * @param ranker the Ranker of the user before they buy auto-promote
   * @param ladder the ladder with all rankers before they buy auto-promote
   * @param round  the round with all ladders before they buy auto-promote
   */
  public void recordAutoPromote(RankerEntity ranker, LadderEntity ladder, RoundEntity round) {
    RankerRecord rankerRecord = new RankerRecord(ranker, round);
    LadderRecord ladderRecord = new LadderRecord(ladder,
        rankerService.findAllByLadderId(ladder.getId()));

    Map<LadderEntity, List<RankerEntity>> roundState = ladderService.findAllByRound(round).stream()
        .collect(Collectors.toMap(l -> l, l -> rankerService.findAllByLadderId(l.getId())));
    RoundRecord roundRecord = new RoundRecord(round, roundState);
    autoPromoteRecordRepository.save(
        new AutoPromoteRecordEntity(rankerRecord, ladderRecord, roundRecord));
  }

  /**
   * Records the moment before a user promotes to the next ladder.
   *
   * @param ranker the Ranker of the user before they promote to the next ladder
   * @param ladder the ladder with all rankers before they promote to the next ladder
   * @param round  the round with all ladders before they promote to the next ladder
   */
  public void recordPromote(RankerEntity ranker, LadderEntity ladder, RoundEntity round) {
    RankerRecord rankerRecord = new RankerRecord(ranker, round);
    LadderRecord ladderRecord = new LadderRecord(ladder,
        rankerService.findAllByLadderId(ladder.getId()));

    Map<LadderEntity, List<RankerEntity>> roundState = ladderService.findAllByRound(round).stream()
        .collect(Collectors.toMap(l -> l, l -> rankerService.findAllByLadderId(l.getId())));
    RoundRecord roundRecord = new RoundRecord(round, roundState);
    promoteRecordRepository.save(
        new PromoteRecordEntity(rankerRecord, ladderRecord, roundRecord));
  }

  /**
   * Records the moment before a user throws vinegar.
   *
   * @param ranker the Ranker of the user before they throw vinegar
   * @param ladder the ladder with all rankers before they throw vinegar
   * @param round  the round with all ladders before they throw vinegar
   */
  public void recordVinegarThrow(RankerEntity ranker, RankerEntity target, LadderEntity ladder,
      RoundEntity round) {
    RankerRecord rankerRecord = new RankerRecord(ranker, round);
    RankerRecord targetRecord = new RankerRecord(target, round);
    List<RankerEntity> rankers = rankerService.findAllByCurrentLadderNumber(ladder.getNumber());
    RankerRecord secondRecord = new RankerRecord(rankers.get(1), round);
    LadderRecord ladderRecord = new LadderRecord(ladder,
        rankerService.findAllByLadderId(ladder.getId()));

    Map<LadderEntity, List<RankerEntity>> roundState = ladderService.findAllByRound(round).stream()
        .collect(Collectors.toMap(l -> l, l -> rankerService.findAllByLadderId(l.getId())));
    RoundRecord roundRecord = new RoundRecord(round, roundState);
    throwVinegarRecordRepository.save(
        new ThrowVinegarRecordEntity(rankerRecord, targetRecord, secondRecord, ladderRecord,
            roundRecord));
  }

  public RoundStatisticsEntity getRoundStatistics(Integer number) {
    SeasonEntity currentSeason = seasonService.getCurrentSeason();
    Optional<RoundEntity> round = roundService.findBySeasonAndNumber(currentSeason, number);
    if (round.isEmpty()) {
      return null;
    }

    Optional<RoundStatisticsEntity> statistics = roundStatisticsRepository.findByRoundId(
        round.get().getId());
    return statistics.orElse(null);
  }

  public ActivityAnalysisEntity getActivityAnalysis() {
    return activityAnalysisRepository.findTopByOrderByCreatedOnDesc()
        .orElse(null);
  }
}
