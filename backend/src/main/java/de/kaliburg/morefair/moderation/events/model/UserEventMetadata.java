package de.kaliburg.morefair.moderation.events.model;

import de.kaliburg.morefair.moderation.events.model.dto.UserEventRequest;
import lombok.NonNull;

public record UserEventMetadata(
    @NonNull Boolean isTrusted,
    @NonNull Integer screenX,
    @NonNull Integer screenY) {

  public UserEventMetadata(UserEventRequest request) {
    this(request.getIsTrusted(), request.getScreenX(), request.getScreenY());
  }
}
