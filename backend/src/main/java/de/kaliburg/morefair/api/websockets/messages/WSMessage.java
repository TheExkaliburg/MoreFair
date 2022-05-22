package de.kaliburg.morefair.api.websockets.messages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class WSMessage extends WSEmptyMessage {

  @NonNull
  private String content;

  public WSMessage(@NonNull String uuid, @NonNull String content) {
    super(uuid);
    this.content = content;
  }
}
