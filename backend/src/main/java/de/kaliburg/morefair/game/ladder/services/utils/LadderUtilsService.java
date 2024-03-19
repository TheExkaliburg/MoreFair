package de.kaliburg.morefair.game.ladder.services.utils;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import org.springframework.lang.NonNull;

public interface LadderUtilsService {

  Integer getRequiredRankerCountToUnlock(LadderEntity ladder);

  boolean isLadderUnlocked(@NonNull LadderEntity ladder);

  boolean isLadderPromotable(@NonNull LadderEntity ladder);
}
