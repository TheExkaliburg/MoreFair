package de.kaliburg.morefair.moderation.events.services;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class UserEventPersistConfig {

  public static final String EXECUTOR_NAME = "userEventPersistExecutor";

  /**
   * Single worker, so UserEvent writes never compete with the game for database connections and
   * keep their order. Pending writes are finished on shutdown.
   */
  @Bean(name = EXECUTOR_NAME)
  public Executor userEventPersistExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setThreadNamePrefix("user-event-persist-");
    executor.setCorePoolSize(1);
    executor.setMaxPoolSize(1);
    executor.setQueueCapacity(10_000);
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(10);
    return executor;
  }
}
