package de.kaliburg.morefair.events.types;

/**
 * All the different types of events a user or the server can create.
 */
public enum EventType {
  /**
   * A User is supposed to increase their bias and loose all their points
   */
  BIAS,
  MULTI,
  PROMOTE,
  ASSHOLE,
  VINEGAR,
  JOIN,
  NAME_CHANGE,
  SOFT_RESET_POINTS,
  AUTO_PROMOTE,
  RESET,
  BAN,
  FREE,
  MUTE,
  CONFIRM,
  MOD,
  SYSTEM_MESSAGE
}