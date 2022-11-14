package de.kaliburg.morefair.api.websockets.messages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@RequiredArgsConstructor
public class WsMetaMessage extends WsMessage {

  @NonNull
  private String metadata;

  public WsMetaMessage(@NonNull String uuid, @NonNull String content, @NonNull String metadata) {
    super(uuid, content);
    this.metadata = metadata;
  }
}
