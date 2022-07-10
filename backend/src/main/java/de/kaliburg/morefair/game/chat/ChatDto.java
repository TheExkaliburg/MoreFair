package de.kaliburg.morefair.game.chat;

import de.kaliburg.morefair.FairConfig;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ChatDto {

  private final Integer currentChatNumber;
  private final List<MessageDto> messages = new ArrayList<>();

  public ChatDto(ChatEntity chat, FairConfig config) {
    currentChatNumber = chat.getNumber();

    List<MessageEntity> sortedMessages = chat.getMessages();
    sortedMessages.sort((o1, o2) -> o2.getCreatedOn().compareTo(o1.getCreatedOn()));
    sortedMessages = sortedMessages.subList(0, Math.min(30, sortedMessages.size()));
    for (MessageEntity m : sortedMessages) {
      messages.add(new MessageDto(m, config));
    }
  }
}
