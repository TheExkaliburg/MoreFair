package de.kaliburg.morefair.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Event<T extends Enum<T>> {

  @NonNull
  private Enum<T> eventType;
  private Long accountId;
  private Object data;

  public Event(@NonNull Enum<T> eventType, Long accountId) {
    this.eventType = eventType;
    this.accountId = accountId;
  }

  public Event(@NonNull Enum<T> eventType, Object data) {
    this.eventType = eventType;
    this.data = data;
  }
}
