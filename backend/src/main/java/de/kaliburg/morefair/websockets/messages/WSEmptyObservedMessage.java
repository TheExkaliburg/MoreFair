package de.kaliburg.morefair.websockets.messages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class WSEmptyObservedMessage extends WSEmptyMessage {
    @NonNull
    private String event;

    public WSEmptyObservedMessage(@NonNull String uuid, @NonNull String event) {
        super(uuid);
        this.event = event;
    }
}
