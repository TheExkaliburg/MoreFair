package de.kaliburg.morefair.api.websockets.messages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class WsObservedMessage extends WsMessage {

  @NonNull
  private String event;

  public WsObservedMessage(@NonNull String uuid, @NonNull String content, @NonNull String event) {
    super(uuid, content);
    this.event = event;
  }
}
