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

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Log4j2
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    private final static Integer MAX_CONNECTIONS_PER_MINUTE = 5;
    private final LoadingCache<Integer, Integer> connectionsPerIpAddress;

    public CustomHandshakeHandler() {
        super();

        connectionsPerIpAddress = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build(new CacheLoader<>() {
            @Override
            public @Nullable Integer load(@NonNull Integer s) throws Exception {
                return 0;
            }
        });
    }

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String ipString = request.getHeaders().getOrDefault("x-forwarded-for", List.of(request.getRemoteAddress().getHostName())).get(0);
        Integer ip = 0;
        try {
            ip = new BigInteger(InetAddress.getByName(ipString).getAddress()).intValue();
        } catch (UnknownHostException e) {
            log.error(e);
            e.printStackTrace();
        }

        if (isMaximumConnectionsPerMinuteExceeded(ip)) {
            return null;
        }

        UUID uuid = UUID.randomUUID();
        log.trace("Determining user for session {} with ip {} as {}", request.getURI().toString(), ip, uuid);
        return new StompPrincipal(uuid.toString(), ip);
    }

    private boolean isMaximumConnectionsPerMinuteExceeded(Integer ipAddress) {
        Integer requests;
        requests = connectionsPerIpAddress.get(ipAddress);
        if (requests != null) {
            if (requests >= MAX_CONNECTIONS_PER_MINUTE) {
                return true;
            }
        } else {
            requests = 0;
        }
        requests++;
        connectionsPerIpAddress.put(ipAddress, requests);
        return false;
    }
}
