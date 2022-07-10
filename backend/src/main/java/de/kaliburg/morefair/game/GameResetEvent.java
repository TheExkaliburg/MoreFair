package de.kaliburg.morefair.game;

import org.springframework.context.ApplicationEvent;

public class GameResetEvent extends ApplicationEvent {

  public GameResetEvent(Object source) {
    super(source);
  }
}
