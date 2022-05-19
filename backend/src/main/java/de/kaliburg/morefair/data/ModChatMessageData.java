package de.kaliburg.morefair.data;

import de.kaliburg.morefair.api.FairController;
import de.kaliburg.morefair.game.message.MessageEntity;
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

    public ModChatMessageData(MessageEntity message) {
        this.message = StringEscapeUtils.unescapeJava(message.getMessage());
        this.username = StringEscapeUtils.unescapeJava(message.getAccount().getUsername());
        this.assholeTag = FairController.ASSHOLE_TAGS.get(message.getAccount().getTimesAsshole());
        this.accountId = message.getAccount().getId();
        this.timeCreated = message.getCreatedOn().format(DateTimeFormatter.ofPattern("EE HH:mm"));
        this.ladderNumber = message.getLadder().getNumber();
    }
}
