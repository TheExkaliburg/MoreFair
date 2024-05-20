package de.kaliburg.morefair.game.vinegar.services;

import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.LadderEventType;
import de.kaliburg.morefair.game.vinegar.model.VinegarFeud;
import de.kaliburg.morefair.game.vinegar.model.VinegarThrowEntity;
import java.util.List;
import java.util.Optional;

public interface VinegarThrowService {

  Optional<VinegarThrowEntity> throwVinegar(Event<LadderEventType> event);

  List<VinegarThrowEntity> findListOfPastThrows(Long accountId);

  List<VinegarFeud> findCurrentFeuds(Long accountId);

}
