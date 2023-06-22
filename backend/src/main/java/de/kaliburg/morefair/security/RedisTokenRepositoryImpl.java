package de.kaliburg.morefair.security;


import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Repository;

/**
 * An Implementation of the <code>PersistentTokenRepository</code> using Redis as a database. See:
 * <a href="https://gist.github.com/romanoffs/728effb5e88c9b4e210b1889cdfe6153">GitHub</a>
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisTokenRepositoryImpl implements PersistentTokenRepository {

  public static final int TOKEN_VALID_DAYS = 28;
  private static final StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
  private static final String USERNAME = "username";
  private static final String TOKEN = "token";
  private static final String LAST_USED_DATE = "last_used_date";
  private static final String NAME_SPACE = "rememberMe";
  private final RedisTemplate<String, String> redisTemplate;

  @Value("${spring.session.redis.namespace}")
  private String redisNamespace;

  private String generateKey(String series) {
    return redisNamespace + ":" + NAME_SPACE + ":" + series;
  }

  @Override
  public void createNewToken(PersistentRememberMeToken token) {
    val key = generateKey(token.getSeries());

    val data = Map.of(
        USERNAME, token.getUsername(),
        TOKEN, token.getTokenValue(),
        LAST_USED_DATE, String.valueOf(token.getDate().getTime())
    );

    redisTemplate.opsForHash().putAll(key, data);
    redisTemplate.expire(key, TOKEN_VALID_DAYS, TimeUnit.DAYS);
  }

  @Override
  @Nullable
  public void updateToken(String series, String tokenValue, Date lastUsed) {
    val key = generateKey(series);

    val data = Map.of(
        TOKEN, tokenValue,
        LAST_USED_DATE, String.valueOf(lastUsed.getTime())
    );

    redisTemplate.opsForHash().putAll(key, data);
    redisTemplate.expire(key, TOKEN_VALID_DAYS, TimeUnit.DAYS);
  }

  @Override
  public PersistentRememberMeToken getTokenForSeries(String seriesId) {
    val key = generateKey(seriesId);
    val hashValues = redisTemplate.opsForHash().multiGet(key, Arrays.asList(USERNAME, TOKEN,
        LAST_USED_DATE));
    val username = hashValues.get(0);
    val tokenValue = hashValues.get(1);
    val date = hashValues.get(2);

    if (ObjectUtils.anyNull(username, tokenValue, date)) {
      return null;
    }

    val timestamp = Long.parseLong(String.valueOf(date));
    val time = new Date(timestamp);
    return new PersistentRememberMeToken(String.valueOf(username), seriesId,
        String.valueOf(tokenValue), time);
  }

  @Override
  public void removeUserTokens(String username) {
    byte[] hashKey = stringRedisSerializer.serialize(USERNAME);
    Objects.requireNonNull(redisTemplate.getConnectionFactory());
    val redisConnection = redisTemplate.getConnectionFactory().getConnection();
    try (val cursor = redisConnection.scan(
        ScanOptions.scanOptions().match(generateKey("*")).count(1024).build())) {
      while (cursor.hasNext()) {
        byte[] key = cursor.next();
        byte[] hashValue = redisConnection.hGet(key, hashKey);
        String storeName = stringRedisSerializer.deserialize(hashValue);
        if (username.equals(storeName)) {
          redisConnection.expire(key, 0L);
          return;
        }
      }
    }
  }
}
