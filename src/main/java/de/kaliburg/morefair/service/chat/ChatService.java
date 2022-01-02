package de.kaliburg.morefair.service.chat;

import de.kaliburg.morefair.dto.chat.ChatDTO;
import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.entity.Ladder;
import de.kaliburg.morefair.entity.chat.Message;
import de.kaliburg.morefair.repository.LadderRepository;
import de.kaliburg.morefair.repository.chat.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ChatService {
    private final MessageRepository messageRepository;
    private final LadderRepository ladderRepository;

    public ChatService(MessageRepository messageRepository, LadderRepository ladderRepository) {
        this.messageRepository = messageRepository;
        this.ladderRepository = ladderRepository;
    }

    public ChatDTO getChat(int ladderNum) {
        return ladderRepository.findByNumber(ladderNum).convertToChatDTO();
    }

    public void writeMessage(Account account, Integer ladderNum, String messageString) {
        Ladder ladder = ladderRepository.findByNumber(ladderNum);
        Message message = new Message(UUID.randomUUID(), account, messageString, ladder);
        messageRepository.save(message);
    }
}
