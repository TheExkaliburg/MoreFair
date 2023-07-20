package de.kaliburg.morefair.game.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public enum ChatType {
  GLOBAL,
  LADDER(true),
  SYSTEM;

  @Getter
  private boolean parameterized = false;

  ChatType(boolean parameterized) {
    this.parameterized = parameterized;
  }
}
