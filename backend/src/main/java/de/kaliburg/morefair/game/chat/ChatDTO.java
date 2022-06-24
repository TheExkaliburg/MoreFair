package de.kaliburg.morefair.game.chat;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ChatDTO {

  private final Integer currentChatNumber;
  private final List<MessageDTO> messages = new ArrayList<>();

  public ChatDTO(ChatEntity chat) {
    currentChatNumber = chat.getNumber();
    /*
    List<MessageEntity> sortedMessages = ladder.getMessages();
    sortedMessages.sort((o1, o2) -> o2.getCreatedOn().compareTo(o1.getCreatedOn()));
    sortedMessages = sortedMessages.subList(0, Math.min(30, sortedMessages.size()));
    for (MessageEntity m : sortedMessages) {
      messages.add(m.convertToDTO());
    }
    */
  }
}
