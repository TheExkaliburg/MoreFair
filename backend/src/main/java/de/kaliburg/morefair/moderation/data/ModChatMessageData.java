package de.kaliburg.morefair.moderation.data;

import de.kaliburg.morefair.FairController;
import de.kaliburg.morefair.chat.Message;
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
  private final Integer ladderNumber;

  public ModChatMessageData(Message message) {
    this.message = StringEscapeUtils.unescapeJava(message.getMessage());
    this.username = StringEscapeUtils.unescapeJava(message.getAccount().getUsername());
    this.assholeTag = FairController.ASSHOLE_TAGS.get(
        Math.min(message.getAccount().getTimesAsshole(),
            FairController.ASSHOLE_TAGS.size() - 1));
    this.accountId = message.getAccount().getId();
    this.timeCreated = message.getCreatedOn().format(DateTimeFormatter.ofPattern("EE HH:mm"));
    this.ladderNumber = message.getLadder().getNumber();
  }
}
