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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
@Log4j2
public class MessageService {
    private final MessageRepository messageRepository;
    private final LadderRepository ladderRepository;

    @Getter
    private Map<Integer, Ladder> chats = new HashMap<>();

    public MessageService(MessageRepository messageRepository, LadderRepository ladderRepository) {
        this.messageRepository = messageRepository;
        this.ladderRepository = ladderRepository;
    }

    public Ladder findLadderWithChat(Ladder ladder) {
        return ladderRepository.findLadderByUUIDWithMessage(ladder.getUuid());
    }

    public Ladder addChat(Ladder ladder) {
        Ladder result = findLadderWithChat(ladder);
        chats.putIfAbsent(ladder.getNumber(), result);
        return result;
    }

    @PostConstruct
    public void init() {
        ladderRepository.findAllLaddersJoinedWithMessages().forEach(l -> chats.put(l.getNumber(), l));
        for (Ladder l : chats.values()) {
            if (l.getMessages().size() > 30) {
                List<Message> messages = l.getMessages();
                messages.sort(Comparator.comparing(Message::getCreatedOn));
                l.setMessages(messages.subList(0, 30));
            }
        }
    }

    // Every Minute
    @Scheduled(initialDelay = 60000, fixedRate = 60000)
    public void syncWithDB() {
        log.debug("Saving Chats...");
        deleteAllMessages();

        for (Ladder l : chats.values()) {
            saveAllMessages(l.getMessages());
        }
        log.trace("Chats are saved!");
    }

    @Transactional
    public void deleteAllMessages() {
        messageRepository.deleteAll();
    }

    @Transactional
    public void saveAllMessages(List<Message> messages) {
        messageRepository.saveAll(messages);
    }

    //TODO: Manage the Chat better
    public ChatDTO getChat(int ladderNum) {
        return chats.get(ladderNum).convertToChatDTO();
    }

    public Message writeMessage(Account account, Integer ladderNum, String messageString) {
        Ladder ladder = chats.get(ladderNum);
        Message message = new Message(UUID.randomUUID(), account, messageString, ladder);
        List<Message> messages = ladder.getMessages();
        messages.add(0, message);
        if (messages.size() > 30) messages.remove(messages.size() - 1);
        ladder.setMessages(messages);
        return message;
    }
}
