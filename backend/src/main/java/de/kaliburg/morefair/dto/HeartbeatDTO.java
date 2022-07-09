package de.kaliburg.morefair.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class HeartbeatDTO {

  @NonNull
  Double delta = 1d;

}
