package de.kaliburg.morefair.data;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.chat.MessageEntity;
import java.time.format.DateTimeFormatter;
import lombok.Data;

@Data
public class ModChatMessageData {

  private final String message;
  private final String username;
  private final String assholeTag;
  private final Long accountId;
  private final String timeCreated;
  private final Integer ladderNumber = 0;

  public ModChatMessageData(MessageEntity message, FairConfig config) {
    this.message = message.getMessage();
    this.username = message.getAccount().getDisplayName();
    this.assholeTag = config.getAssholeTag(message.getAccount().getAssholeCount());
    this.accountId = message.getAccount().getId();
    this.timeCreated = message.getCreatedOn().format(DateTimeFormatter.ofPattern("EE HH:mm"));
  }
}
