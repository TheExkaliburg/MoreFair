package de.kaliburg.morefair.events;

import lombok.Data;
import lombok.NonNull;

@Data
public class Event {
    @NonNull
    private EventType eventType;
    @NonNull
    private Long accountId;
    private Object data;
    
}
