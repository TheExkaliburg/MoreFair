package de.kaliburg.morefair.data;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.chat.MessageEntity;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ModChatData {

  private final List<ModChatMessageData> messages = new ArrayList<>();

  public ModChatData(ArrayList<MessageEntity> messages, FairConfig config) {
    ((List<MessageEntity>) messages).sort(
        (o1, o2) -> o2.getCreatedOn().compareTo(o1.getCreatedOn()));
    for (MessageEntity m : messages) {
      this.messages.add(new ModChatMessageData(m, config));
    }
  }
}
