package de.kaliburg.morefair.data;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.chat.model.MessageEntity;
import de.kaliburg.morefair.chat.model.dto.MessageDto;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ModChatDto {

  private final List<MessageDto> messages = new ArrayList<>();

  public ModChatDto(List<MessageEntity> messages, FairConfig config) {
    messages.sort((o1, o2) -> o2.getCreatedOn().compareTo(o1.getCreatedOn()));
    for (MessageEntity m : messages) {
      this.messages.add(new MessageDto(m, config));
    }
  }
}
