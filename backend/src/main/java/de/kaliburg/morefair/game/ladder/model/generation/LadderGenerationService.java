package de.kaliburg.morefair.game.ladder.model.generation;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.generation.generator.LadderGenerator;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class LadderGenerationService {

  private final List<LadderGenerator> ladderGenerators;

  @PostConstruct
  public void checkAmbiguity() {
    Map<RoundType, List<LadderGenerator>> groupedByType = ladderGenerators.stream()
        .collect(Collectors.groupingBy(LadderGenerator::getSpecialRoundType));

    groupedByType.forEach((roundType, generators) -> {
      if (generators.size() > 1) {
        String generatorNames = generators.stream()
            .map(generator -> generator.getClass().getSimpleName())
            .collect(Collectors.joining(", "));
        log.warn(
            "Ambiguity detected for RoundType '{}'. Multiple LadderGenerators found: ({}). Consider ensuring only one generator is registered to avoid unpredictable results.",
            roundType, generatorNames
        );
      }
    });
  }

  public List<LadderEntity> generateLaddersForRound(RoundEntity round) {
    LadderGenerationContext context = new LadderGenerationContext(round);

    var generator = getGenerator(round.getTypes());

    return generator.generateLadders(context);
  }

  private LadderGenerator getGenerator(Set<RoundType> types) {
    // Create a map for efficient generator lookup
    Map<RoundType, LadderGenerator> map = ladderGenerators.stream()
        .collect(Collectors.toMap(LadderGenerator::getSpecialRoundType, g -> g));

    // Return the first matching generator for types, falling back to DEFAULT if no match is found
    return types.stream()
        .map(map::get)
        .filter(Objects::nonNull)
        .findFirst()
        .or(() -> Optional.ofNullable(map.get(RoundType.DEFAULT)))
        .orElseThrow(() -> new IllegalStateException("No default LadderGenerator found"));
  }
}
