package de.kaliburg.morefair.game.ladder.model.generation;

import static org.assertj.core.api.Assertions.assertThat;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import de.kaliburg.morefair.utils.FairTest;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@FairTest
class LadderGenerationServiceTest {

  @Autowired
  FairConfig fairConfig;
  @Autowired
  private LadderGenerationService ladderGenerationService;

  @Test
  void generateLadder_defaultRound_correctLadderList() {
    RoundEntity round = generateRound(EnumSet.of(RoundType.DEFAULT));

    List<LadderEntity> list = ladderGenerationService.generateLaddersForRound(round);

    assertThat(list)
        .hasSize(round.getAssholeLadderNumber() + 1);
    assertThat(list.get(list.size() - 2).getTypes())
        .contains(LadderType.ASSHOLE, LadderType.NO_AUTO)
        .doesNotContain(LadderType.DEFAULT);
    assertThat(list.get(list.size() - 1).getTypes())
        .contains(LadderType.END)
        .doesNotContain(LadderType.DEFAULT);

    // Check Scaling
    assertThat(list.get(0).getScaling()).isEqualTo(1);
    assertThat(list.get(list.size() - 2).getScaling()).isEqualTo(round.getAssholeLadderNumber());
  }

  @Test
  void generateLadder_special100Round_correctLadderList() {
    RoundEntity round = generateRound(EnumSet.of(RoundType.SPECIAL_100, RoundType.REVERSE_SCALING));

    List<LadderEntity> list = ladderGenerationService.generateLaddersForRound(round);

    assertThat(list)
        .hasSize(101);
    assertThat(list.get(list.size() - 2).getTypes())
        .contains(LadderType.NO_AUTO)
        .doesNotContain(LadderType.ASSHOLE, LadderType.DEFAULT);
    assertThat(list.get(list.size() - 1).getTypes())
        .contains(LadderType.END)
        .doesNotContain(LadderType.DEFAULT);

    // Check Scaling
    assertThat(list.get(0).getScaling()).isEqualTo(100);
    assertThat(list.get(list.size() - 2).getScaling()).isEqualTo(1);
  }

  private RoundEntity generateRound(Set<RoundType> types) {
    return RoundEntity.builder()
        .id(1L)
        .uuid(UUID.randomUUID())
        .assholeLadderNumber(15)
        .basePointsRequirement(fairConfig.getBasePointsToPromote())
        .number(1)
        .createdOn(OffsetDateTime.now())
        .types(types)
        .percentageOfAdditionalAssholes(0.5f)
        .seasonId(1L)
        .build();
  }
}