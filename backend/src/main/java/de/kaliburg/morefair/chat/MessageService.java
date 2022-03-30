package de.kaliburg.morefair.chat;

import de.kaliburg.morefair.account.entity.Account;
import de.kaliburg.morefair.account.events.AccountServiceEvent;
import de.kaliburg.morefair.dto.ChatDTO;
import de.kaliburg.morefair.ladder.Ladder;
import de.kaliburg.morefair.ladder.LadderRepository;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.Semaphore;

@Service
@Log4j2
public class MessageService implements ApplicationListener<AccountServiceEvent> {
    private final MessageRepository messageRepository;
    private final LadderRepository ladderRepository;
    @Getter
    private Map<Integer, Ladder> chats = new HashMap<>();
    private Semaphore chatSem = new Semaphore(1);

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
            if (l.getMessages().size() > 50) {
                List<Message> messages = l.getMessages();
                messages.sort(Comparator.comparing(Message::getCreatedOn));
                l.setMessages(messages.subList(0, 50));
            }
        }
    }

    // Every Minute
    @Scheduled(initialDelay = 60000, fixedRate = 60000)
    @PreDestroy
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

    public Message writeMessage(Account account, Integer ladderNum, String messageString, String metadata) {
        Ladder ladder = chats.get(ladderNum);
        Message message = new Message(UUID.randomUUID(), account, messageString, ladder);

        if(!StringUtils.isBlank(metadata)){
            message.setMetadata(metadata);
        }

        List<Message> messages = ladder.getMessages();
        messages.add(0, message);
        if (messages.size() > 30) messages.remove(messages.size() - 1);
        ladder.setMessages(messages);
        return message;
    }

    public ArrayList<Message> getAllMessages() {
        ArrayList<Message> result = new ArrayList<>();
        chats.values().forEach(c -> result.addAll(c.getMessages()));
        return result;
    }

    @Override
    public void onApplicationEvent(AccountServiceEvent event) {
        if (event.getEventType().equals(AccountServiceEvent.AccountServiceEventType.UPDATE)) {
            for (Ladder ladder : chats.values()) {
                for (Message message : chats.get(ladder.getNumber()).getMessages()) {
                    if (message.getAccount().getId().equals(event.getAccount().getId())) {
                        message.setAccount(event.getAccount());
                    }
                }
            }
        }

        if (event.getEventType().equals(AccountServiceEvent.AccountServiceEventType.BAN)
                || event.getEventType().equals(AccountServiceEvent.AccountServiceEventType.MUTE)) {
            try {
                chatSem.acquire();
                try {
                    chats.values().forEach(ladder -> {
                        List<Message> messages = new ArrayList<>(ladder.getMessages());

                        messages.forEach(message -> {
                            if (message.getAccount().getId().equals(event.getAccount().getId())) {
                                ladder.getMessages().remove(message);
                            }
                        });
                    });
                } finally {
                    chatSem.release();
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
