package de.kaliburg.morefair.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Event<T extends Enum<T>> {

  @NonNull
  private Enum<T> eventType;
  @NonNull
  private Long accountId;
  private Object data;
}
