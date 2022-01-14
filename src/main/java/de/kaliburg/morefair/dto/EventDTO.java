package de.kaliburg.morefair.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class EventDTO {
    @NonNull
    private EventType eventType;
    @NonNull
    private Long accountId;

    public enum EventType {
        BIAS, MULTI, PROMOTE, ASSHOLE, VINEGAR
    }
}
