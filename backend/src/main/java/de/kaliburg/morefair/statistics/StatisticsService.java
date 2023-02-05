package de.kaliburg.morefair.statistics;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.MoreFairApplication;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.game.round.LadderEntity;
import de.kaliburg.morefair.game.round.RankerEntity;
import de.kaliburg.morefair.game.round.RoundEntity;
import de.kaliburg.morefair.game.round.RoundService;
import de.kaliburg.morefair.game.round.dto.RoundResultsDto;
import de.kaliburg.morefair.statistics.records.AutoPromoteRecordEntity;
import de.kaliburg.morefair.statistics.records.AutoPromoteRecordRepository;
import de.kaliburg.morefair.statistics.records.BiasRecordEntity;
import de.kaliburg.morefair.statistics.records.BiasRecordRepository;
import de.kaliburg.morefair.statistics.records.LadderRecord;
import de.kaliburg.morefair.statistics.records.LoginRecordEntity;
import de.kaliburg.morefair.statistics.records.LoginRecordRepository;
import de.kaliburg.morefair.statistics.records.MultiRecordEntity;
import de.kaliburg.morefair.statistics.records.MultiRecordRepository;
import de.kaliburg.morefair.statistics.records.PromoteRecordEntity;
import de.kaliburg.morefair.statistics.records.PromoteRecordRepository;
import de.kaliburg.morefair.statistics.records.RankerRecord;
import de.kaliburg.morefair.statistics.records.RoundRecord;
import de.kaliburg.morefair.statistics.records.ThrowVinegarRecordEntity;
import de.kaliburg.morefair.statistics.records.ThrowVinegarRecordRepository;
import de.kaliburg.morefair.statistics.results.RoundStatisticsEntity;
import de.kaliburg.morefair.statistics.results.RoundStatisticsRepository;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class StatisticsService {

  private final LoginRecordRepository loginRecordRepository;
  private final BiasRecordRepository biasRecordRepository;
  private final MultiRecordRepository multiRecordRepository;
  private final AutoPromoteRecordRepository autoPromoteRecordRepository;
  private final PromoteRecordRepository promoteRecordRepository;
  private final ThrowVinegarRecordRepository throwVinegarRecordRepository;
  private final RoundStatisticsRepository roundStatisticsRepository;

  @Autowired
  @Lazy
  private final RoundService roundService;

  private final FairConfig config;
  private final Cache<Integer, RoundResultsDto> roundResultsCache =
      Caffeine.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).maximumSize(10).build();
  private final LoadingCache<Long, Boolean> hasStartedStatisticsJobRecently =
      Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build(number -> false);

  @Value("${spring.profiles.active}")
  private String activeProfile;
  @Value("${spring.datasource.password}")
  private String sqlPassword;
  @Value("${spring.datasource.username}")
  private String sqlUsername;

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
    RankerRecord rankerRecord = new RankerRecord(ranker);
    LadderRecord ladderRecord = new LadderRecord(ladder);
    RoundRecord roundRecord = new RoundRecord(round);
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
    RankerRecord rankerRecord = new RankerRecord(ranker);
    LadderRecord ladderRecord = new LadderRecord(ladder);
    RoundRecord roundRecord = new RoundRecord(round);
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
    RankerRecord rankerRecord = new RankerRecord(ranker);
    LadderRecord ladderRecord = new LadderRecord(ladder);
    RoundRecord roundRecord = new RoundRecord(round);
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
    RankerRecord rankerRecord = new RankerRecord(ranker);
    LadderRecord ladderRecord = new LadderRecord(ladder);
    RoundRecord roundRecord = new RoundRecord(round);
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
    RankerRecord rankerRecord = new RankerRecord(ranker);
    RankerRecord targetRecord = new RankerRecord(target);
    List<RankerEntity> rankers = ladder.getRankers();
    RankerRecord secondRecord = new RankerRecord(rankers.get(1));
    LadderRecord ladderRecord = new LadderRecord(ladder);
    RoundRecord roundRecord = new RoundRecord(round);
    throwVinegarRecordRepository.save(
        new ThrowVinegarRecordEntity(rankerRecord, targetRecord, secondRecord, ladderRecord,
            roundRecord));
  }

  /**
   * Sends a request to start the General Analytics for the Server.
   */
  @PostConstruct
  @Scheduled(cron = "0 */30 * * * *")
  public void startGeneralAnalytics() {
    startAnalytics("GeneralAnalytics", null);
  }

  /**
   * Sends a request to start the Round Statistics for the Server.
   */
  public void startRoundStatistics(long roundId) {
    Boolean request = hasStartedStatisticsJobRecently.get(roundId);
    if (request != null && !request) {
      log.info("starting roundStatistics for roundId: {}", roundId);
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
      sparkProperties.addProperty("spark.master", "spark://85.214.71.14:7077");
      sparkProperties.addProperty("spark.app.name", "Spark Live Test");
      sparkProperties.addProperty("spark.executor.memory", "8g");
      sparkProperties.addProperty("spark.driver.memory", "8g");
      sparkProperties.addProperty("spark.driver.cores", "2");
      jsonBody.add("sparkProperties", sparkProperties);

      // Add the other properties
      String jarPath = MoreFairApplication.class.getProtectionDomain().getCodeSource().getLocation()
          .getPath();
      if (jarPath.contains("!")) {
        jarPath = jarPath.substring(0, jarPath.lastIndexOf("!"));
        jarPath = jarPath.substring(0, jarPath.lastIndexOf("/"));
      }
      File jarFile = new File(jarPath);
      String actualPath = jarFile.getParentFile().getParent();
      log.debug(actualPath);

      jsonBody.addProperty("appResource", actualPath + "/spark.jar");
      jsonBody.addProperty("clientSparkVersion", "3.3.1");
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
      String url = "http://85.214.71.14:6066/v1/submissions/create";

      // Send the request and get the response
      CompletableFuture.runAsync(() -> {
        try {
          restTemplate.postForObject(url, jsonString, String.class);
        } catch (Exception e) {
          log.error(e.getMessage());
        }
      });
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  public RoundResultsDto getRoundResults(Integer number) {
    RoundResultsDto result = roundResultsCache.getIfPresent(number);
    if (result == null) {
      RoundEntity round = roundService.find(number);
      if (round == null) {
        return null;
      }
      result = new RoundResultsDto(round, config);
      roundResultsCache.put(number, result);
    }
    return result;
  }

  public RoundStatisticsEntity getRoundStatistics(Integer number) {
    RoundEntity round = roundService.find(number);
    if (round == null) {
      return null;
    }

    Optional<RoundStatisticsEntity> statistics = roundStatisticsRepository.findByRoundId(
        round.getId());
    if (statistics.isEmpty()) {
      startRoundStatistics(round.getId());
    }

    return statistics.orElse(null);
  }
}
