package de.kaliburg.morefair.api.utils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.api.websockets.StompPrincipal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class RequestThrottler {

  private static final Integer MAX_MESSAGES = 3;

  private final LoadingCache<Integer, Boolean> hasCreatedAccountRecently;
  private final LoadingCache<UUID, Integer> hasPostedMessageRecently;

  public RequestThrottler() {
    this.hasCreatedAccountRecently = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES)
        .build(integer -> false);
    this.hasPostedMessageRecently = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS)
        .build(integer -> 0);
  }

  public boolean canCreateAccount(StompPrincipal principal) {
    Boolean request;
    Integer ipAddress = principal.getIpAddress();
    request = hasCreatedAccountRecently.get(ipAddress);
    if (request != null) {
      if (request) {
        hasCreatedAccountRecently.asMap().remove(ipAddress);
        hasCreatedAccountRecently.put(ipAddress, true);
        return false;
      }
    }
    hasCreatedAccountRecently.put(ipAddress, true);
    return true;
  }

  public boolean canPostMessage(UUID uuid) {
    Integer requests;
    requests = hasPostedMessageRecently.get(uuid);
    if (requests != null) {
      if (requests >= MAX_MESSAGES) {
        hasPostedMessageRecently.asMap().remove(uuid);
        hasPostedMessageRecently.put(uuid, requests);
        return false;
      }
    } else {
      requests = 0;
    }
    requests++;
    hasPostedMessageRecently.put(uuid, requests);
    return true;
  }
}
