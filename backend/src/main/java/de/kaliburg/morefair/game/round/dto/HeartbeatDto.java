package de.kaliburg.morefair.game.round.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class HeartbeatDto {

  @NonNull
  Double delta = 1d;
}
