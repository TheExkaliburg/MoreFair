package de.kaliburg.morefair.statistics.services;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.statistics.results.ActivityAnalysisEntity;
import de.kaliburg.morefair.statistics.results.RoundStatisticsEntity;

public interface StatisticsService {

  void recordLogin(AccountEntity account);

  RoundStatisticsEntity getRoundStatistics(Integer roundNumber);

  ActivityAnalysisEntity getActivityAnalysis();

}
