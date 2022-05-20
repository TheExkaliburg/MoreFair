package de.kaliburg.morefair.game.chat.message;

import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.account.entity.AccountEntity;
import de.kaliburg.morefair.account.events.AccountServiceEvent;
import de.kaliburg.morefair.api.utils.WSUtils;
import de.kaliburg.morefair.dto.ChatDTO;
import de.kaliburg.morefair.game.round.ladder.LadderEntity;
import de.kaliburg.morefair.game.round.ladder.LadderRepository;
import lombok.Getter;
import lombok.NonNull;
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

import static de.kaliburg.morefair.api.ChatController.CHAT_UPDATE_DESTINATION;

@Service
@Log4j2
public class MessageService implements ApplicationListener<AccountServiceEvent> {
    private final MessageRepository messageRepository;
    private final AccountService accountService;
    private final LadderRepository ladderRepository;
    private final WSUtils wsUtils;
    @Getter
    private Map<Integer, LadderEntity> chats = new HashMap<>();
    private Semaphore chatSem = new Semaphore(1);

    public MessageService(MessageRepository messageRepository, LadderRepository ladderRepository,
            AccountService accountService, WSUtils wsUtils) {
        this.messageRepository = messageRepository;
        this.ladderRepository = ladderRepository;
        this.accountService = accountService;
        this.wsUtils = wsUtils;
    }

    public LadderEntity findLadderWithChat(LadderEntity ladder) {
        return ladderRepository.findLadderByUUIDWithMessage(ladder.getUuid());
    }

    public LadderEntity addChat(LadderEntity ladder) {
        LadderEntity result = findLadderWithChat(ladder);
        chats.putIfAbsent(ladder.getNumber(), result);
        return result;
    }

    @PostConstruct
    public void init() {
        ladderRepository.findAllLaddersJoinedWithMessages().forEach(l -> chats.put(l.getNumber(), l));
        for (LadderEntity l : chats.values()) {
            if (l.getMessages().size() > 50) {
                List<MessageEntity> messages = l.getMessages();
                messages.sort(Comparator.comparing(MessageEntity::getCreatedOn));
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
        for (LadderEntity l : chats.values()) {
            saveAllMessages(l.getMessages());
        }
        log.trace("Chats are saved!");
    }

    @Transactional
    public void deleteAllMessages() {
        messageRepository.deleteAll();
    }

    @Transactional
    public void saveAllMessages(List<MessageEntity> messages) {
        messageRepository.saveAll(messages);
    }

    //TODO: Manage the Chat better
    public ChatDTO getChat(int ladderNum) {
        return chats.get(ladderNum).convertToChatDTO();
    }

    public void writeSystemMessage(@NonNull LadderEntity highestLadder, @NonNull String messageString) {
        try {
            AccountEntity systemMessager = accountService.findOwnerAccount();
            log.debug("SystemMessager is " + (systemMessager != null ? systemMessager.getUsername() : " null"));
            if (systemMessager != null) {
                chats.values().forEach(ladder -> {
                    MessageEntity answer = writeMessage(systemMessager, ladder.getNumber(), messageString);
                    wsUtils.convertAndSendToAll(CHAT_UPDATE_DESTINATION + ladder, answer.convertToDTO());
                });
            }
        } catch (RuntimeException re) {
            log.error("Error processing System Message: " + messageString, re);
        }
    }

    public MessageEntity writeMessage(AccountEntity account, Integer ladderNum, String messageString) {
        return writeMessage(account, ladderNum, messageString, null);
    }

    public MessageEntity writeMessage(AccountEntity account, Integer ladderNum, String messageString, String metadata) {
        LadderEntity ladder = chats.get(ladderNum);
        MessageEntity message = new MessageEntity(UUID.randomUUID(), account, messageString, ladder);

        if (!StringUtils.isBlank(metadata)) {
            message.setMetadata(metadata);
        }

        List<MessageEntity> messages = ladder.getMessages();
        messages.add(0, message);
        if (messages.size() > 30)
            messages.remove(messages.size() - 1);
        ladder.setMessages(messages);
        return message;
    }

    public ArrayList<MessageEntity> getAllMessages() {
        ArrayList<MessageEntity> result = new ArrayList<>();
        chats.values().forEach(c -> result.addAll(c.getMessages()));
        return result;
    }

    @Override
    public void onApplicationEvent(AccountServiceEvent event) {
        if (event.getEventType().equals(AccountServiceEvent.AccountServiceEventType.UPDATE)) {
            for (LadderEntity ladder : chats.values()) {
                for (MessageEntity message : chats.get(ladder.getNumber()).getMessages()) {
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
                        List<MessageEntity> messages = new ArrayList<>(ladder.getMessages());

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
