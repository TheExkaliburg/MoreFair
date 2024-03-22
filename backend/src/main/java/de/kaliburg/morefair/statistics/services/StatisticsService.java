package de.kaliburg.morefair.statistics.services;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.statistics.results.ActivityAnalysisEntity;
import de.kaliburg.morefair.statistics.results.RoundStatisticsEntity;

public interface StatisticsService {

  void recordLogin(AccountEntity account);

  RoundStatisticsEntity getRoundStatistics(Integer roundNumber);

  ActivityAnalysisEntity getActivityAnalysis();

  void recordBias(RankerEntity ranker, LadderEntity ladder, RoundEntity round);

  void recordMulti(RankerEntity ranker, LadderEntity ladder, RoundEntity round);

  void recordAutoPromote(RankerEntity ranker, LadderEntity ladder, RoundEntity round);

  void recordPromote(RankerEntity ranker, LadderEntity ladder, RoundEntity round);

  void recordVinegarThrow(RankerEntity ranker, RankerEntity target, LadderEntity ladder,
      RoundEntity round);
}
