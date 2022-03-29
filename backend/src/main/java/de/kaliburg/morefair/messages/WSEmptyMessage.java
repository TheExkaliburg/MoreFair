package de.kaliburg.morefair.messages;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class WSEmptyMessage {
    @NonNull
    private String uuid;

    public WSEmptyMessage(@NonNull String uuid) {
        this.uuid = uuid;
    }
}
