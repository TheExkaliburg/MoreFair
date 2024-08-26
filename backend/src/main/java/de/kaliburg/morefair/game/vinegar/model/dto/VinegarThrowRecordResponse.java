package de.kaliburg.morefair.game.vinegar.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder
@AllArgsConstructor
public class VinegarThrowRecordResponse {

  @Singular
  private List<ThrowRecordResponse> throwRecords;
}
