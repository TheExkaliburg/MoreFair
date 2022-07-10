package de.kaliburg.morefair.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class HeartbeatDto {

  @NonNull
  Double delta = 1d;

}
