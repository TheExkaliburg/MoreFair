package de.kaliburg.morefair.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "game")
@ConfigurationPropertiesScan
public class GameConfig {

  /**
   * tester.
   */
  private final String test;
}
