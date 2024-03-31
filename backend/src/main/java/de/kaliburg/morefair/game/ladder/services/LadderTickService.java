package de.kaliburg.morefair.game.ladder.services;

import de.kaliburg.morefair.core.concurrency.CriticalRegion;


public interface LadderTickService {

  // TODO: Make sure everything that is using this, is also really needing this
  CriticalRegion getSemaphore();


}
