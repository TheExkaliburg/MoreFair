package de.kaliburg.morefair.api.websockets.messages;

import de.kaliburg.morefair.moderation.events.model.dto.UserEventRequest;
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
  private UserEventRequest event;
  @NonNull
  private String metadata;


}
