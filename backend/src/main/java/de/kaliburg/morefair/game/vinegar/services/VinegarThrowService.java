package de.kaliburg.morefair.game.vinegar.services;

import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.LadderEventType;
import de.kaliburg.morefair.game.vinegar.model.VinegarThrowEntity;
import java.util.List;
import java.util.Optional;

public interface VinegarThrowService {

  Optional<VinegarThrowEntity> throwVinegar(Event<LadderEventType> event);

  List<VinegarThrowEntity> findVinegarThrowsOfCurrentSeason(Long accountId);

  List<VinegarThrowEntity> findVinegarThrowsOfCurrentRound(Long accountId);
}
