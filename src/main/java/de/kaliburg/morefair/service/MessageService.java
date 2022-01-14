package de.kaliburg.morefair.service;

import de.kaliburg.morefair.dto.ChatDTO;
import de.kaliburg.morefair.persistence.entity.Account;
import de.kaliburg.morefair.persistence.entity.Ladder;
import de.kaliburg.morefair.persistence.entity.Message;
import de.kaliburg.morefair.persistence.repository.LadderRepository;
import de.kaliburg.morefair.persistence.repository.MessageRepository;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
@Log4j2
public class MessageService {
    private final MessageRepository messageRepository;
    private final LadderRepository ladderRepository;

    @Getter
    private List<Ladder> chats = new ArrayList<>();

    public MessageService(MessageRepository messageRepository, LadderRepository ladderRepository) {
        this.messageRepository = messageRepository;
        this.ladderRepository = ladderRepository;
    }

    @PostConstruct
    public void init() {
        messageRepository.deleteAll();
        chats = ladderRepository.findAllLaddersJoinedWithMessages().stream().toList();
        for (Ladder l : chats) {
            if (l.getMessages().size() > 30) {
                List<Message> messages = l.getMessages();
                messages.sort(Comparator.comparing(Message::getCreatedOn));
                l.setMessages(messages.subList(0, 30));
            }
        }
    }

    // Every Minute
    @Scheduled(fixedRate = 60000)
    public void syncWithDB() {
        // TODO: Sync with DB
        //ladderRepository.saveAll(ladderMemory);
        log.debug("Saving Chats...");
    }

    public ChatDTO getChat(int ladderNum) {
        Optional<Ladder> optional = chats.stream().filter(l -> l.getNumber() == ladderNum).findFirst();
        Ladder ladder = optional.get();
        return ladder.convertToChatDTO();
    }

    public Message writeMessage(Account account, Integer ladderNum, String messageString) {
        Ladder ladder = chats.stream().filter(l -> l.getNumber() == ladderNum).findFirst().get();
        Message message = new Message(UUID.randomUUID(), account, messageString, ladder);
        List<Message> messages = ladder.getMessages();
        messages.add(0, message);
        if (messages.size() > 30) messages.remove(messages.size() - 1);
        ladder.setMessages(messages);
        return message;
    }
}
