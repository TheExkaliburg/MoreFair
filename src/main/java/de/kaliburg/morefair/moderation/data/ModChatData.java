package de.kaliburg.morefair.moderation.data;

import de.kaliburg.morefair.persistence.entity.Message;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ModChatData {
    private final List<ModChatMessageData> messages = new ArrayList<>();

    public ModChatData(ArrayList<Message> messages) {
        ((List<Message>) messages).sort((o1, o2) -> o2.getCreatedOn().compareTo(o1.getCreatedOn()));
        for (Message m : messages) {
            this.messages.add(new ModChatMessageData(m));
        }
    }
}
