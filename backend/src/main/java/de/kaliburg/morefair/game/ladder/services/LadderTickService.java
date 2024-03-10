package de.kaliburg.morefair.game.ladder.services;

import de.kaliburg.morefair.core.concurrency.CriticalRegion;


public interface LadderTickService {

  CriticalRegion getSemaphore();


}
