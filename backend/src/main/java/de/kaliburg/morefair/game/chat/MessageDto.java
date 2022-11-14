package de.kaliburg.morefair.game.chat;

import de.kaliburg.morefair.FairConfig;
import lombok.Data;

@Data
public class MessageDto {

  private final String message;
  private final String username;
  private final Long id;
  private final Long timestamp;
  private final String tag;
  private final Integer assholePoints;
  private final String metadata;

  public MessageDto(MessageEntity message, FairConfig config) {
    this.tag = config.getAssholeTag(message.getAccount().getAssholeCount());
    this.assholePoints = message.getAccount().getAssholePoints();
    this.message = message.getMessage();
    this.username = message.getAccount().getDisplayName();
    this.id = message.getAccount().getId();
    this.timestamp = message.getCreatedOn().toEpochSecond();
    this.metadata = message.getMetadata();
  }
}
