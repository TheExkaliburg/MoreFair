package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.events.Event;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class HeartbeatDTO {
    @NonNull
    List<Event> events;
    @NonNull
    Double secondsPassed = 1d;

}
