package de.kaliburg.morefair.data;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.chat.dto.MessageDto;
import de.kaliburg.morefair.game.chat.MessageEntity;
import lombok.Getter;

@Getter
public class ModMessageDto extends MessageDto {

  private final Integer chatNumber;
  private final boolean isDeleted;

  public ModMessageDto(MessageEntity message, FairConfig config) {
    super(message, config);
    this.chatNumber = message.getChat().getNumber();
    this.isDeleted = message.isDeleted();
  }
}
