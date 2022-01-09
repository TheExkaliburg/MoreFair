package de.kaliburg.morefair.websockets;

import com.github.javafaker.Faker;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledPushMessages {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final Faker faker;

    public ScheduledPushMessages(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        faker = new Faker();
    }

    @Scheduled(fixedRate = 5000)
    public void sendMessage() {
        simpMessagingTemplate.convertAndSend("/topic/pushmessages", new WSMessageAnswer<>(faker.chuckNorris().fact()));
    }
}
