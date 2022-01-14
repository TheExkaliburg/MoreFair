package de.kaliburg.morefair.dto;

import lombok.Data;

@Data
public class EventDTO {
    private EventType eventType;
    private Long rankerId;

    public enum EventType {
        BIAS, MULTI, PROMOTE, ASSHOLE, VINEGAR
    }
}
