package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.chat.Message;
import de.kaliburg.morefair.chat.MessageDTO;
import de.kaliburg.morefair.ladder.Ladder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatDTO {
    private final Integer currentChatNumber;
    private final List<MessageDTO> messages = new ArrayList<>();

    public ChatDTO(Ladder ladder) {
        currentChatNumber = ladder.getNumber();
        List<Message> sortedMessages = ladder.getMessages();
        sortedMessages.sort((o1, o2) -> o2.getCreatedOn().compareTo(o1.getCreatedOn()));
        sortedMessages = sortedMessages.subList(0, Math.min(30, sortedMessages.size()));
        for (Message m : sortedMessages) {
            messages.add(m.convertToDTO());
        }
    }
}
