package de.kaliburg.morefair.events.data;

import lombok.Data;
import lombok.NonNull;

@Data
public class VinegarData {

  @NonNull
  private String amount;
  @NonNull
  private boolean success = false;
  @NonNull
  private Long targetId;
}
