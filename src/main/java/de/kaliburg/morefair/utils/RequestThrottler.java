package de.kaliburg.morefair.utils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.websockets.StompPrincipal;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Log4j2
public class RequestThrottler {
    private final LoadingCache<Integer, Boolean> hasCreatedAccountRecently;

    public RequestThrottler() {
        this.hasCreatedAccountRecently = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES)
                .build(integer -> false);
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
}
