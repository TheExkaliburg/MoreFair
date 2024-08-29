package de.kaliburg.morefair.chat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SuggestionDto {

  private Long accountId;
  private String displayName;
}
