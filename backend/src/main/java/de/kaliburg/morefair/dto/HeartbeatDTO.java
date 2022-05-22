package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.events.Event;
import java.util.List;
import lombok.Data;
import lombok.NonNull;

@Data
public class HeartbeatDTO {

  @NonNull
  List<Event> events;
  @NonNull
  Double secondsPassed = 1d;

}
