package de.kaliburg.morefair.game.ladder.model.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class LadderTickDto {

  @NonNull
  Double delta = 1d;
}
