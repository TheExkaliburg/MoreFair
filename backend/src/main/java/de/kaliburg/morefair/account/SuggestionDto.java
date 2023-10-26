package de.kaliburg.morefair.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionDto {

  private Long accountId;
  private String displayName;
}
