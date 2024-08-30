package de.kaliburg.morefair.game.ladder.model.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
public class LadderTickDto {

  @NonNull
  @Builder.Default
  Integer delta = 1;
}
