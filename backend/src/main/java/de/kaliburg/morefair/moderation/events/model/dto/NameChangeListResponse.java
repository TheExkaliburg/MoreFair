package de.kaliburg.morefair.moderation.events.model.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NameChangeListResponse {

  private List<NameChangeResponse> list;

  @Data
  @Builder
  public static class NameChangeResponse {

    private String displayName;
    private String currentName;
    private Long accountId;
    private Long timestamp;
    
  }
}
