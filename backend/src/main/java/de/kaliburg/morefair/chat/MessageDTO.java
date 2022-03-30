package de.kaliburg.morefair.chat;

import de.kaliburg.morefair.FairController;
import de.kaliburg.morefair.chat.Message;
import lombok.Data;
import org.apache.commons.text.StringEscapeUtils;

import java.time.format.DateTimeFormatter;

@Data
public class MessageDTO {
    private final String message;
    private final String username;
    private final Integer timesAsshole;
    private final Long accountId;
    private final String timeCreated;
    private final String assholeTag;
    private final String metadata;

    public MessageDTO(Message message) {
        this.timesAsshole = message.getAccount().getTimesAsshole();
        this.assholeTag = FairController.ASSHOLE_TAGS.get(message.getAccount().getTimesAsshole());
        this.message = StringEscapeUtils.unescapeJava(message.getMessage());
        this.username = StringEscapeUtils.unescapeJava(message.getAccount().getUsername());
        this.accountId = message.getAccount().getId();
        this.timeCreated = message.getCreatedOn().format(DateTimeFormatter.ofPattern("EE HH:mm"));
        this.metadata = message.getMetadata();
    }
}
