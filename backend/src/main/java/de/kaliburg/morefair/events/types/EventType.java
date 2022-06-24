package de.kaliburg.morefair.events.types;

/**
 * All the different types of events a user or the server can create.
 */
public enum EventType {
  /**
   * A User is supposed to increase their bias and loose all their points
   */
  BUY_BIAS,
  BUY_MULTI,
  PROMOTE,
  ASSHOLE,
  THROW_VINEGAR,
  JOIN,
  NAME_CHANGE,
  SOFT_RESET_POINTS,
  BUY_AUTO_PROMOTE,
  RESET,
  BAN,
  FREE,
  MUTE,
  CONFIRM,
  MOD,
  SYSTEM_MESSAGE
}