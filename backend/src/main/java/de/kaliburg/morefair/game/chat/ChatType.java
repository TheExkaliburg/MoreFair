package de.kaliburg.morefair.game.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public enum ChatType {
  GLOBAL,
  LADDER(true),
  ANNOUNCEMENT;

  @Getter
  private boolean parameterized = true;

  private ChatType(boolean parameterized) {
    this.parameterized = parameterized;
  }
}
