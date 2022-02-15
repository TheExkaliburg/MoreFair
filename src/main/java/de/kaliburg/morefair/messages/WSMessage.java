package de.kaliburg.morefair.messages;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class WSMessage {
    @NonNull
    private String uuid;
    @NonNull
    private String content;
    @NonNull
    private Object event;
}
