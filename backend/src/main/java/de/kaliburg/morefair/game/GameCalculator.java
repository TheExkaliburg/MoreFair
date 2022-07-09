package de.kaliburg.morefair.game;

import de.kaliburg.morefair.dto.HeartbeatDto;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Heartbeat of the Game and does all the calculations every tick.
 */
public class GameCalculator {

  private static final double NANOS_IN_SECONDS = TimeUnit.SECONDS.toNanos(1);

  private Map<Integer, HeartbeatDto> heartbeatDTOMap = new HashMap<>();
}
