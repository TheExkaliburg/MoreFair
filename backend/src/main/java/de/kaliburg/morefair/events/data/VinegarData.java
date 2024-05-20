package de.kaliburg.morefair.events.data;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class VinegarData {

  @NonNull
  private String amount;
  @NonNull
  private Integer percentage;
  @NonNull
  private VinegarSuccessType success;
  @NonNull
  private Long targetId;

  public enum VinegarSuccessType {
    SHIELDED, SHIELD_DEFENDED, DEFENDED, SUCCESS, DOUBLE_SUCCESS
  }
}
