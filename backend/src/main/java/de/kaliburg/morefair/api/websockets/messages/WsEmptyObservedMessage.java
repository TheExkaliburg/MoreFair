package de.kaliburg.morefair.api.websockets.messages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class WsEmptyObservedMessage extends WsEmptyMessage {

  @NonNull
  private String event;

  public WsEmptyObservedMessage(@NonNull String uuid, @NonNull String event) {
    super(uuid);
    this.event = event;
  }
}
