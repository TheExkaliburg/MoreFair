package de.kaliburg.morefair.events.data;

import lombok.Data;
import lombok.NonNull;

@Data
public class VinegarData {

  @NonNull
  private String amount;
  private boolean success = false;
  private long targetId;
}
