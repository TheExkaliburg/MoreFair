package de.kaliburg.morefair.data;

import de.kaliburg.morefair.game.message.MessageEntity;
import java.time.format.DateTimeFormatter;
import lombok.Data;
import org.apache.commons.text.StringEscapeUtils;

@Data
public class ModChatMessageData {

  private final String message;
  private final String username;
  private final String assholeTag;
  private final Long accountId;
  private final String timeCreated;
  private final Integer ladderNumber = 0;

  public ModChatMessageData(MessageEntity message) {
    this.message = StringEscapeUtils.unescapeJava(message.getMessage());
    this.username = StringEscapeUtils.unescapeJava(message.getAccount().getUsername());
    this.assholeTag = de.kaliburg.morefair.FairController.ASSHOLE_TAGS.get(
        Math.min(message.getAccount().getTimesAsshole(),
            FairController.ASSHOLE_TAGS.size() - 1));
    this.accountId = message.getAccount().getId();
    this.timeCreated = message.getCreatedOn().format(DateTimeFormatter.ofPattern("EE HH:mm"));
  }
}
