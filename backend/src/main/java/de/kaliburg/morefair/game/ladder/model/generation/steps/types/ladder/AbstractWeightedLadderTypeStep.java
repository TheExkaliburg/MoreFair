package de.kaliburg.morefair.game.ladder.model.generation.steps.types.ladder;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationContext;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationStep;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import de.kaliburg.morefair.utils.RandomUtils;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An abstract base class responsible for implementing ladder generation steps that use weighted
 * probabilities to determine the types of ladders. This class provides common functionality for
 * managing and modifying the weight mappings for ladder types during the ladder generation
 * process.
 * <p>
 * Subclasses must override the {@link AbstractWeightedLadderTypeStep#getWeights()} method to define
 * the initial weight mappings for ladder types. Additionally, subclasses can customize the behavior
 * of ladder generation by overriding
 * {@link AbstractWeightedLadderTypeStep#handleRoundTypes(RoundType, Map)},
 * {@link AbstractWeightedLadderTypeStep#handlePreviousLadder(LadderEntity, Map,
 * LadderGenerationContext)},
 * {@link AbstractWeightedLadderTypeStep#handlePreviousLadderType(LadderType, Map,
 * LadderGenerationContext)} and
 * {@link AbstractWeightedLadderTypeStep#handleExistingLadderTypes(LadderEntity, Map)}.
 */
public abstract class AbstractWeightedLadderTypeStep implements LadderGenerationStep {

  /**
   * Retrieves the weights associated with different ladder types. These weights are used to
   * influence ladder generation and behavior in the game. The map's keys represent the ladder
   * types, and the values represent the associated weights as doubles.
   *
   * @return a map containing ladder types as keys and their corresponding weights as values
   */
  public abstract Map<LadderType, Double> getWeights();

  @Override
  public List<LadderEntity> apply(List<LadderEntity> ladders,
      LadderGenerationContext context) {

    Map<LadderType, Double> baseWeights = new EnumMap<>(getWeights());
    context.getRoundEntity().getTypes().stream()
        .sorted(RoundType.comparator())
        .forEach(type -> handleRoundTypes(type, baseWeights));

    LadderEntity lastLadder = null;
    for (LadderEntity ladder : ladders) {
      var weights = new EnumMap<>(baseWeights);
      handlePreviousLadder(lastLadder, weights, context);
      handleExistingLadder(ladder, weights);

      LadderType ladderType = RandomUtils.rollFromLookupTable(weights);
      ladder.getTypes().add(ladderType);
      lastLadder = ladder;
    }

    return ladders;
  }

  /**
   * Handles adjustments or custom behavior for processing specific round types and their associated
   * ladder type weights. This method is designed to be overridden by subclasses to modify the
   * default behavior.
   *
   * @param roundType the type of the current round being processed
   * @param weights   the mapping of ladder types to their corresponding weights, which can be
   *                  adjusted
   */
  protected void handleRoundTypes(RoundType roundType, final Map<LadderType, Double> weights) {
    // Do nothing by default
  }

  /**
   * Handles adjustments or custom behavior for processing {@link LadderType LadderTypes} that already exist.
   * For example, from a previous generation step.
   * This method is designed to be overridden by subclasses to modify the default behavior.
   *
   * @param ladderType the {@link LadderType}
   * @param weights a map containing {@link LadderType} as keys and their associated adjustment weights as values
   */
  protected void handleExistingLadderType(LadderType ladderType, final Map<LadderType, Double> weights) {
    // Do nothing by default
  }

  /**
   * Handles adjustments or custom behavior for processing a given ladder entity and its associated
   * types. For example, from a previous generation step.
   * This method is designed to be overridden by subclasses to modify the default behavior.
   *
   * @param ladder  the {@link LadderEntity} instance to process, which may contain associated
   *                ladder types; if null, the method performs no operation
   * @param weights a map containing {@link LadderType} as keys and their associated adjustment
   *                weights as values; used to manage or influence behavior for the ladder types
   */
  protected void handleExistingLadder(LadderEntity ladder, final Map<LadderType, Double> weights) {
    if (ladder == null || weights == null) {
      return;
    }

    ladder.getTypes().stream()
        .sorted()
        .forEach(type -> handleExistingLadderType(type, weights));
  }

  /**
   * Handles the processing of a given ladder entity and its associated types by applying specific
   * adjustments or behaviors based on the provided weight mappings. If the ladder or weights are
   * null, the method exits without performing any actions.
   *
   * @param previousLadder the {@link LadderEntity} instance to process, which may contain
   *                       associated ladder types; if null, the method performs no operation
   * @param weights        a map containing {@link LadderType} as keys and their associated
   *                       adjustment weights as values; used to manage or influence behavior for
   *                       the ladder types
   */
  protected void handlePreviousLadder(LadderEntity previousLadder,
      final Map<LadderType, Double> weights,
      final LadderGenerationContext context) {
    if (previousLadder == null || weights == null) {
      return;
    }

    previousLadder.getTypes().stream()
        .sorted()
        .forEach(type -> handlePreviousLadderType(type, weights, context));
  }

  /**
   * Modifies the weight of the previous ladder type during ladder generation, based on specific
   * conditions. If the round type includes {@code RoundType.CHAOS} and a previous ladder type is
   * defined, the weight of the previous ladder type is halved.
   *
   * @param previousLadderType the type of the previously processed ladder; may be null
   * @param weights            a map of ladder types to their respective weights, which may be
   *                           altered by this method
   * @param context            the context of the ladder generation process, containing additional
   *                           information such as round entity and other relevant data
   */
  protected void handlePreviousLadderType(LadderType previousLadderType,
      final Map<LadderType, Double> weights,
      final LadderGenerationContext context) {
    Set<RoundType> roundTypes = context.getRoundEntity().getTypes();
    if (roundTypes.contains(RoundType.CHAOS) && previousLadderType != null) {
      weights.computeIfPresent(previousLadderType, (k, v) -> v / 2);
    }
  }
}
