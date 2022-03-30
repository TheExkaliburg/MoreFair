package de.kaliburg.morefair.websockets.messages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class WSMetaMessage extends WSMessage {
    @NonNull
    private String metadata;

    public WSMetaMessage(@NonNull String uuid, @NonNull String content, @NonNull String metadata) {
        super(uuid, content);
        this.metadata = metadata;
    }
}
