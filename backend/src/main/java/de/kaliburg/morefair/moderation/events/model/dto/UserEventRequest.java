package de.kaliburg.morefair.moderation.events.model.dto;

import lombok.Data;

@Data
public class UserEventRequest {

  private Boolean isTrusted;
  private Integer screenX;
  private Integer screenY;
}
