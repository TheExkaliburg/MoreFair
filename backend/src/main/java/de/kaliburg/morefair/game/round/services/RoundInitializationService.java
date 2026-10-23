package de.kaliburg.morefair.game.round.services;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationService;
import de.kaliburg.morefair.game.ladder.services.repositories.LadderRepository;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import de.kaliburg.morefair.game.round.services.repositories.RoundRepository;
import java.util.Comparator;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
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

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void backfillLadders(RoundEntity round) {
    var existing = ladderRepository.findByRound(round.getId()).stream()
        .sorted(Comparator.comparingInt(LadderEntity::getNumber))
        .toList();
    if (!existing.isEmpty() && existing.getLast().getTypes().contains(LadderType.END)
        && (round.getTypes().contains(RoundType.SPECIAL_100)
        || (existing.size() >= 2
        && existing.get(existing.size() - 2).hasType(LadderType.ASSHOLE)
        && existing.get(existing.size() - 2).getNumber() + 1 == existing.getLast().getNumber()))) {
      return;
    }

    var ladders = new TreeMap<Integer, LadderEntity>();
    ladderGenerationService.generateLaddersForRound(round)
        .forEach(ladder -> ladders.put(ladder.getNumber(), ladder));
    existing.forEach(ladder -> ladders.put(ladder.getNumber(), ladder));
    ladderRepository.saveAllAndFlush(ladders.values());
    log.info("Backfilled ladders for round {}: preserved {}, added {}", round.getId(),
        existing.size(), ladders.size() - existing.size());
  }

}
