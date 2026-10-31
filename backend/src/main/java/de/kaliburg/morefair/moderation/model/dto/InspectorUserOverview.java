package de.kaliburg.morefair.moderation.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class InspectorUserOverview {

  private String displayName;
  private Long messageLogCount;
  private Long eventLogCount;
  private Long auditLogCount;
  private Long nameHistoryCount;
  private Long altAccountsCount;

}
