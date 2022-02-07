package de.kaliburg.morefair.moderation.data;

import de.kaliburg.morefair.FairController;
import de.kaliburg.morefair.chat.Message;
import lombok.Data;
import org.apache.commons.text.StringEscapeUtils;

import java.time.format.DateTimeFormatter;

@Data
public class ModChatMessageData {
    private final String message;
    private final String username;
    private final String assholeTag;
    private final Long accountId;
    private final String timeCreated;
    private final Integer ladderNumber;

    public ModChatMessageData(Message message) {
        this.message = StringEscapeUtils.unescapeJava(message.getMessage());
        this.username = StringEscapeUtils.unescapeJava(message.getAccount().getUsername());
        this.assholeTag = FairController.ASSHOLE_TAGS.get(message.getAccount().getTimesAsshole());
        this.accountId = message.getAccount().getId();
        this.timeCreated = message.getCreatedOn().format(DateTimeFormatter.ofPattern("HH:mm"));
        this.ladderNumber = message.getLadder().getNumber();
    }
}
