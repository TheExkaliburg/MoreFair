package de.kaliburg.morefair.game.chat.dto;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.chat.ChatType;
import de.kaliburg.morefair.game.chat.MessageEntity;
import java.time.ZoneOffset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MessageDto {

  private final String message;
  private final String username;
  private final Long accountId;
  private final Long timestamp;
  private final String tag;
  private final Integer assholePoints;
  private final String metadata;
  private final Boolean isMod;
  private final ChatType chatType;
  private final Integer ladderNumber;

  public MessageDto(MessageEntity message, FairConfig config) {
    this.tag = config.getAssholeTag(message.getAccount().getAssholeCount());
    this.assholePoints = message.getAccount().getAssholePoints();
    this.message = message.getMessage();
    this.username = message.getAccount().getDisplayName();
    this.accountId = message.getAccount().getId();
    this.timestamp = message.getCreatedOn().withOffsetSameInstant(ZoneOffset.UTC).toEpochSecond();
    this.metadata = message.getMetadata();
    this.isMod = message.getAccount().isMod();
    this.chatType = message.getChat().getType();
    this.ladderNumber = message.getChat().getNumber();
  }
}
