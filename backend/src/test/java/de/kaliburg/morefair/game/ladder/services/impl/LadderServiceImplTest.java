package de.kaliburg.morefair.game.ladder.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationService;
import de.kaliburg.morefair.game.ladder.services.repositories.LadderRepository;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.services.RoundInitializationService;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.game.round.services.repositories.RoundRepository;
import java.math.BigInteger;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LadderServiceImplTest {

  @Mock
  private LadderRepository ladderRepository;
  @Mock
  private RoundRepository roundRepository;
  @Mock
  private RoundService roundService;
  @Mock
  private LadderGenerationService generator;
  private LadderServiceImpl ladderService;
  private RoundEntity round;

  @BeforeEach
  void setup() {
    round = RoundEntity.builder()
        .id(1L)
        .seasonId(1L)
        .number(1)
        .assholeLadderNumber(1)
        .basePointsRequirement(BigInteger.TEN)
        .percentageOfAdditionalAssholes(50.f)
        .build();
    ladderService = new LadderServiceImpl(ladderRepository, roundService,
        new RoundInitializationService(roundRepository, ladderRepository, generator));
  }

  @Test
  void reloadLoadsPersistedLaddersWithoutRegenerating() {
    LadderEntity first = ladder(1);
    LadderEntity end = ladder(2);
    when(roundService.getCurrentRound()).thenReturn(round);
    when(ladderRepository.findByRound(1L)).thenReturn(Set.of(first, end));

    ladderService.reloadLadders();

    assertThat(ladderService.findCurrentLadderWithNumber(1)).containsSame(first);
    assertThat(ladderService.findCurrentLadderWithNumber(2)).containsSame(end);
    verifyNoInteractions(generator, roundRepository);
  }

  @Test
  void reloadRejectsEmptyOrPartialRoundsWithoutRegenerating() {
    when(roundService.getCurrentRound()).thenReturn(round);
    when(ladderRepository.findByRound(1L)).thenReturn(Set.of(), Set.of(ladder(1)));

    for (int attempt = 0; attempt < 2; attempt++) {
      assertThatThrownBy(ladderService::reloadLadders)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Automatic migration/repair is not implemented");
    }
    verifyNoInteractions(generator, roundRepository);
  }

  @Test
  void inventoryDoesNotDependOnCachedLadderNumbers() {
    when(ladderRepository.findByRound(1L)).thenReturn(Set.of(ladder(2), ladder(1)));

    assertThat(ladderService.findAllByRound(round))
        .extracting(LadderEntity::getNumber).containsExactly(1, 2);
  }

  @Test
  void cacheMissLoadsPersistedLadderWithoutRegenerating() {
    LadderEntity first = ladder(1);
    when(roundService.getCurrentRound()).thenReturn(round);
    when(ladderRepository.findByRoundAndNumber(1L, 1)).thenReturn(Optional.of(first));
    when(ladderRepository.findById(1L)).thenReturn(Optional.of(first));

    assertThat(ladderService.findCurrentLadderWithNumber(1)).containsSame(first);
    verifyNoInteractions(generator, roundRepository);
  }

  @Test
  void legacyCreationFailsExplicitlyInsteadOfUsingOldGenerator() {
    assertThatThrownBy(() -> ladderService.createCurrentLadder(3))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("VOID recovery is not implemented");
    verifyNoInteractions(ladderRepository, generator);
  }

  private LadderEntity ladder(int number) {
    return LadderEntity.builder()
        .id((long) number)
        .roundId(round.getId())
        .number(number)
        .scaling(number)
        .basePointsToPromote(BigInteger.TEN)
        .types(EnumSet.of(number == 1 ? LadderType.ASSHOLE : LadderType.END))
        .build();
  }
}
