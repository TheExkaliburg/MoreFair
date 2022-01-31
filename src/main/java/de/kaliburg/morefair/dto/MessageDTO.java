package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.persistence.entity.Message;
import lombok.Data;
import org.apache.commons.text.StringEscapeUtils;

@Data
public class MessageDTO {
    private final String message;
    private final String username;
    private final Integer timesAsshole;
    private final Long accountId;
    private final Long timestamp
    public MessageDTO(Message message) {
        this.timesAsshole = message.getAccount().getTimesAsshole();
        this.message = StringEscapeUtils.unescapeJava(message.getMessage());
        this.username = StringEscapeUtils.unescapeJava(message.getAccount().getUsername());
        this.accountId = message.getAccount().getId();
        this.timestamp = message.createdOn;
    }
}
