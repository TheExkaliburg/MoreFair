package de.kaliburg.morefair.statistics;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.kaliburg.morefair.account.AccountEntity;
import java.util.concurrent.CompletableFuture;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
@Slf4j
public class StatisticsService {

  private final LoginRepository loginRepository;

  public void login(AccountEntity account) {
    loginRepository.save(new LoginEntity(account));
  }

  @PostConstruct
  @Scheduled(cron = "0 */10 * * * *")
  public void startAnalytics() {
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
      jsonBody.addProperty("appResource", "/bin/morefair-staging/spark.jar");
      jsonBody.addProperty("clientSparkVersion", "3.3.1");
      jsonBody.addProperty("mainClass", "Login");

      // Add the "environmentVariables" property
      JsonObject environmentVariables = new JsonObject();
      environmentVariables.addProperty("SPARK_ENV_LOADED", "1");
      jsonBody.add("environmentVariables", environmentVariables);

      // Add the "appArgs" property
      JsonArray appArgs = new JsonArray();
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
}
