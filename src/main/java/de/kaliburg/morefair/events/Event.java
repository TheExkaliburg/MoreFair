package de.kaliburg.morefair.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Event {
    @NonNull
    private EventType eventType;
    @NonNull
    private Long accountId;
    private Object data;

}
