package de.kaliburg.morefair.events.data;

import lombok.Data;
import lombok.NonNull;

@Data
public class JoinData {

  @NonNull
  private String username;
  @NonNull
  private String tag;
  @NonNull
  private Integer assholePoints;
}
