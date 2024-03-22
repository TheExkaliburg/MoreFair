package de.kaliburg.morefair.statistics.services.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.round.RoundService;
import de.kaliburg.morefair.game.round.model.RoundEntity;
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
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
@Slf4j
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

  private final LoadingCache<Long, Boolean> hasStartedStatisticsJobRecently =
      Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build(number -> false);

  @Value("${spring.profiles.active}")
  private String activeProfile;
  @Value("${spring.datasource.password}")
  private String sqlPassword;
  @Value("${spring.datasource.username}")
  private String sqlUsername;

  @Value("${fair.spark.master-url}")
  private String sparkMasterUrl = "";

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

  /**
   * Sends a request to start the General Analytics for the Server.
   */
  @PostConstruct
  @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
  public void startGeneralAnalytics() {
    startAnalytics("GeneralAnalytics", null);
  }

  /**
   * Sends a request to start the Round Statistics for the Server.
   */
  public void startRoundStatistics(long roundId) {
    Boolean request = hasStartedStatisticsJobRecently.get(roundId);
    if (request != null && !request) {
      log.info("Starting RoundStatistics for roundId: {}", roundId);
      startAnalytics("RoundStatistics", roundId);
    }
    hasStartedStatisticsJobRecently.put(roundId, true);
  }

  /**
   * Sends a Request to the local spark-master to start a new job. Sends the html request as a
   * completable future, so it doesn't block the thread.
   *
   * @param mainClass the main Class of the Spark Application that should be run on the cluster
   */
  public void startAnalytics(String mainClass, @Nullable Long currentRoundId) {
    try {
      // Create a JSON object for the request body
      JsonObject jsonBody = new JsonObject();
      jsonBody.addProperty("action", "CreateSubmissionRequest");

      // Add the "sparkProperties" property
      JsonObject sparkProperties = new JsonObject();
      sparkProperties.addProperty("spark.master", "spark://master:7077");
      sparkProperties.addProperty("spark.app.name", "Spark Live Test");
      sparkProperties.addProperty("spark.executor.memory", "8g");
      sparkProperties.addProperty("spark.driver.memory", "8g");
      sparkProperties.addProperty("spark.driver.cores", "2");
      jsonBody.add("sparkProperties", sparkProperties);

      // Add the other properties
      String host = "";
      if (activeProfile.equals("prod")) {
        host = "https://fair.kaliburg.de";
      } else if (activeProfile.equals("staging")) {
        host = "https://fairtest.kaliburg.de";
      }

      jsonBody.addProperty("appResource", host + "/spark.jar");
      jsonBody.addProperty("clientSparkVersion", "3.4.1");
      jsonBody.addProperty("mainClass", mainClass);

      // Add the "environmentVariables" property
      JsonObject environmentVariables = new JsonObject();
      environmentVariables.addProperty("SPARK_ENV_LOADED", "1");
      environmentVariables.addProperty("SQL_USERNAME", sqlUsername);
      environmentVariables.addProperty("SQL_PASSWORD", sqlPassword);
      environmentVariables.addProperty("PROFILE", activeProfile);
      jsonBody.add("environmentVariables", environmentVariables);

      // Add the "appArgs" property
      JsonArray appArgs = new JsonArray();
      if (currentRoundId != null) {
        appArgs.add(currentRoundId);
      }
      jsonBody.add("appArgs", appArgs);

      // Convert the JSON object to a string
      Gson gson = new Gson();
      String jsonString = gson.toJson(jsonBody);

      RestTemplate restTemplate = new RestTemplate();
      String url = sparkMasterUrl + "/api/v1/submissions/create";

      // Send the request and get the response
      CompletableFuture.runAsync(() -> {
        try {
          restTemplate.postForObject(url, jsonString, String.class);
        } catch (Exception e) {
          log.error(e.getMessage());
        }
      });
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  public RoundStatisticsEntity getRoundStatistics(Integer number) {
    SeasonEntity currentSeason = seasonService.getCurrentSeason();
    Optional<RoundEntity> round = roundService.findBySeasonAndNumber(currentSeason, number);
    if (round.isEmpty()) {
      return null;
    }

    Optional<RoundStatisticsEntity> statistics = roundStatisticsRepository.findByRoundId(
        round.get().getId());
    if (statistics.isEmpty()) {
      startRoundStatistics(round.get().getId());
    }

    return statistics.orElse(null);
  }

  public ActivityAnalysisEntity getActivityAnalysis() {
    return activityAnalysisRepository.findTopByOrderByCreatedOnDesc()
        .orElse(null);
  }
}
