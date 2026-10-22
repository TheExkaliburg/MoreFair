package de.kaliburg.morefair.game.round.services;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationService;
import de.kaliburg.morefair.game.ladder.services.repositories.LadderRepository;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.services.repositories.RoundRepository;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoundInitializationService {

  private final RoundRepository roundRepository;
  private final LadderRepository ladderRepository;
  private final LadderGenerationService ladderGenerationService;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public RoundEntity create(RoundEntity round) {
    RoundEntity savedRound = roundRepository.saveAndFlush(round);
    var ladders = ladderGenerationService.generateLaddersForRound(savedRound);
    ladderRepository.saveAllAndFlush(ladders);
    return savedRound;
  }

}
