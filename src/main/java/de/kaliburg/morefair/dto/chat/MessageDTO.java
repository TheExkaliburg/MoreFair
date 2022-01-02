package de.kaliburg.morefair.dto.chat;

import de.kaliburg.morefair.entity.chat.Message;
import lombok.Data;

@Data
public class MessageDTO {
    private final String username;
    private final String message;

    public MessageDTO(Message message) {
        this.message = message.getMessage();
        this.username = message.getAccount().getUsername();
    }
}
