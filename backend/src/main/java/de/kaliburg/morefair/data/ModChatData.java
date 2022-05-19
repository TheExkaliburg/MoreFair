package de.kaliburg.morefair.data;

import de.kaliburg.morefair.game.message.MessageEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ModChatData {
    private final List<ModChatMessageData> messages = new ArrayList<>();

    public ModChatData(ArrayList<MessageEntity> messages) {
        ((List<MessageEntity>) messages).sort((o1, o2) -> o2.getCreatedOn().compareTo(o1.getCreatedOn()));
        for (MessageEntity m : messages) {
            this.messages.add(new ModChatMessageData(m));
        }
    }
}
