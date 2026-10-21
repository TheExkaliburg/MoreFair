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
    validateLadders(savedRound, ladders);
    ladderRepository.saveAllAndFlush(ladders);
    return savedRound;
  }

  public void validateLadders(RoundEntity round, Collection<LadderEntity> ladders) {
    int expectedCount = round.getAssholeLadderNumber() + 1;
    Set<Integer> numbers = ladders.stream()
        .map(LadderEntity::getNumber)
        .collect(Collectors.toSet());

    if (ladders.size() != expectedCount
        || numbers.size() != expectedCount
        || !IntStream.rangeClosed(1, expectedCount).allMatch(numbers::contains)
        || ladders.stream().anyMatch(ladder -> !round.getId().equals(ladder.getRoundId()))) {
      // TODO: Add explicit migration/repair for existing incomplete rounds; never reroll on reload.
      throw new IllegalStateException(
          "Incomplete ladder set for round " + round.getId() + ": expected ladders 1-"
              + expectedCount + ", found " + numbers
              + ". Automatic migration/repair is not implemented.");
    }
  }
}
