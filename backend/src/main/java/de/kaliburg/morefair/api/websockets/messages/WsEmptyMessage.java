package de.kaliburg.morefair.api.websockets.messages;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class WsEmptyMessage {

  @NonNull
  private String uuid;

  public WsEmptyMessage(@NonNull String uuid) {
    this.uuid = uuid;
  }
}
