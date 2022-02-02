package de.kaliburg.morefair.websockets;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.log4j.Log4j2;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Log4j2
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    private LoadingCache<String, Integer> connectionsPerIpAddress;

    public CustomHandshakeHandler() {
        super();

        connectionsPerIpAddress = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build(new CacheLoader<String, Integer>() {
            @Override
            public @Nullable Integer load(@NonNull String s) throws Exception {
                return 0;
            }
        });
    }

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        log.debug(request.getHeaders().get("x-forwarded-for"));
        UUID uuid = UUID.randomUUID();
        log.trace("Determining user for session {} as {}", request.getURI().toString(), uuid);

        return new StompPrincipal(uuid.toString());
    }
}
