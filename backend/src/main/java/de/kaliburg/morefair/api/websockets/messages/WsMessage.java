package de.kaliburg.morefair.api.websockets.messages;

import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class WsMessage {

  @Nullable
  private String content;
  @Nullable
  private String event;
  @NonNull
  private String metadata;


}
