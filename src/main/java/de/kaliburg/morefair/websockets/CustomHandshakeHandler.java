package de.kaliburg.morefair.websockets;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Log4j2
public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        UUID uuid = UUID.randomUUID();
        log.trace("Determining user for session {} as {}", request.getURI().toString(), uuid);

        return new StompPrincipal(uuid.toString());
    }
}
