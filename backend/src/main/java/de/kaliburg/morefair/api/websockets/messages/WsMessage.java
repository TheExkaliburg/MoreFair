package de.kaliburg.morefair.api.websockets.messages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class WsMessage extends WsEmptyMessage {

  @NonNull
  private String content;

  public WsMessage(@NonNull String uuid, @NonNull String content) {
    super(uuid);
    this.content = content;
  }
}
