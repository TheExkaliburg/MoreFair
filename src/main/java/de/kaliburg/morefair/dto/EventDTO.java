package de.kaliburg.morefair.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class EventDTO {
    @NonNull
    private EventType eventType;
    @NonNull
    private Long accountId;
    private JoinDTO joinData;
    private String changedUsername;
    private String vinegarThrown;

    // TODO: Rework the way this DTO transfers additional Data like joinData, changedUsername, vinegarThrown...

    public enum EventType {
        BIAS, MULTI, PROMOTE, ASSHOLE, VINEGAR, JOIN, NAMECHANGE, RESET
    }
}
