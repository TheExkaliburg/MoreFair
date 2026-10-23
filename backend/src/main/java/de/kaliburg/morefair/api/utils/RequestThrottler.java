package de.kaliburg.morefair.api.utils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.account.model.AccountEntity;
import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * The RequestThrottler class enforces rate-limiting policies for specific actions such as account
 * creation, message posting, and querying statistics. It uses in-memory caches to track and
 * validate requests against pre-defined limits, ensuring system resources are not overwhelmed by
 * excessive or abusive usage.
 */
@Component
@Log4j2
public class RequestThrottler {

  private static final int MAX_MESSAGES = 3;
  private static final int MAX_QUERY_REQUESTS_PER_SECOND = 2;
  private static final int MAX_ACCOUNTS_PER_MINUTE = 1;
  private static final int MAX_ACCOUNTS_PER_HOUR = 3;
  private static final int MAX_ACCOUNTS_PER_DAY = 5;

  private final LoadingCache<Integer, Integer> accountCreationLastMinuteCache =
      Caffeine.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build(ip -> 0);

  private final LoadingCache<Integer, Integer> accountCreationLastHourCache =
      Caffeine.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build(ip -> 0);

  private final LoadingCache<Integer, Integer> accountCreationLastDayCache =
      Caffeine.newBuilder().expireAfterAccess(1, TimeUnit.DAYS).build(ip -> 0);

  private final LoadingCache<Integer, Integer> queryStatisticsCache =
      Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build(ip -> 0);

  private final LoadingCache<UUID, Integer> messagePostingCache =
      Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build(uuid -> 0);

  /**
   * Determines whether an account can be created for the given IP address based on rate limits for
   * account creation. The method checks against limits for accounts created in the last minute,
   * hour, and day.
   *
   * @param ipAddress The IP address of the requester attempting to create an account.
   * @return True if the account can be created without exceeding the rate limit, false otherwise.
   */
  public boolean canCreateAccount(Integer ipAddress) {
    if (!updateAndCheckRate(accountCreationLastMinuteCache, ipAddress, MAX_ACCOUNTS_PER_MINUTE)) {
      return false;
    }
    if (!updateAndCheckRate(accountCreationLastHourCache, ipAddress, MAX_ACCOUNTS_PER_HOUR)) {
      return false;
    }
    return updateAndCheckRate(accountCreationLastDayCache, ipAddress, MAX_ACCOUNTS_PER_DAY);
  }

  public boolean canPostMessage(AccountEntity account) {
    return canPostMessage(account.getUuid());
  }

  public boolean canPostMessage(UUID uuid) {
    return updateAndCheckRate(messagePostingCache, uuid, MAX_MESSAGES);
  }

  public boolean canQueryStatistics(Integer ipAddress) {
    return updateAndCheckRate(queryStatisticsCache, ipAddress, MAX_QUERY_REQUESTS_PER_SECOND);
  }

  /**
   * Helper method to update the cache and check if the rate limit is exceeded.
   *
   * @param cache       The cache to update.
   * @param key         The key (IP address or UUID).
   * @param maxRequests The maximum allowed requests within the time frame.
   * @param <T>         The type of the key (either UUID or Integer).
   * @return True if the rate limit is not exceeded, false otherwise.
   */
  private <T extends Serializable> boolean updateAndCheckRate(
      LoadingCache<T, Integer> cache, T key,
      int maxRequests) {
    Integer requests = cache.get(key);
    if (requests == null) {
      requests = 0;
    }

    cache.put(key, requests + 1);
    return requests < maxRequests;
  }
}
