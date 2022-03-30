package de.kaliburg.morefair.utils;

import de.kaliburg.morefair.websockets.messages.WSMessageAnswer;
import de.kaliburg.morefair.websockets.StompPrincipal;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
@Log4j2
public class WSUtils {
    private final SimpMessagingTemplate simpMessagingTemplate;

    private final Set<String> createdAccountRecently = Collections.synchronizedSet(new HashSet<>());

    public WSUtils(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void convertAndSendToUser(SimpMessageHeaderAccessor sha, String dest, Object content) {
        simpMessagingTemplate.convertAndSendToUser(sha.getUser().getName(), dest, new WSMessageAnswer<>(content));
    }

    public void convertAndSendToUser(SimpMessageHeaderAccessor sha, String dest, Object content, HttpStatus status) {
        simpMessagingTemplate.convertAndSendToUser(sha.getUser().getName(), dest, new WSMessageAnswer<>(content, status));
    }

    public void convertAndSendToUser(SimpMessageHeaderAccessor sha, String dest, HttpStatus status) {
        simpMessagingTemplate.convertAndSendToUser(sha.getUser().getName(), dest, new WSMessageAnswer<>("", status));
    }

    public void convertAndSendToUser(UUID uuid, String dest, Object content) {
        simpMessagingTemplate.convertAndSend(dest + uuid.toString(), new WSMessageAnswer<>(content));
    }

    public void convertAndSendToUser(UUID uuid, String dest, Object content, HttpStatus status) {
        simpMessagingTemplate.convertAndSend(dest + uuid.toString(), new WSMessageAnswer<>(content, status));
    }

    public void convertAndSendToUser(UUID uuid, String dest, HttpStatus status) {
        simpMessagingTemplate.convertAndSend(dest + uuid.toString(), new WSMessageAnswer<>("", status));
    }

    public void convertAndSendToAll(String dest, Object content) {
        simpMessagingTemplate.convertAndSend(dest, content);
    }

    public StompPrincipal convertMessageHeaderAccessorToStompPrincipal(SimpMessageHeaderAccessor sha) {
        return ((StompPrincipal) sha.getUser());
    }
}
