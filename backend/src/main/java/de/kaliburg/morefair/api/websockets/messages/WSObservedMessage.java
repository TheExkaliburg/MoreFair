package de.kaliburg.morefair.api.websockets.messages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class WSObservedMessage extends WSMessage {
    @NonNull
    private String event;

    public WSObservedMessage(@NonNull String uuid, @NonNull String content, @NonNull String event) {
        super(uuid, content);
        this.event = event;
    }
}
