package de.kaliburg.morefair.dto;

import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class HeartbeatDTO {
    @NonNull
    List<EventDTO> events;
    @NonNull
    Double secondsPassed = 1d;


}
