package de.kaliburg.morefair.game.round.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationService;
import de.kaliburg.morefair.game.ladder.model.generation.generator.DefaultLadderGenerator;
import de.kaliburg.morefair.game.ladder.services.repositories.LadderRepository;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import de.kaliburg.morefair.game.round.services.repositories.RoundRepository;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoundInitializationServiceTest {

  @Mock
  private RoundRepository roundRepository;
  @Mock
  private LadderRepository ladderRepository;
  @Mock
  private LadderGenerationService ladderGenerationService;
  @InjectMocks
  private RoundInitializationService service;
  @Captor
  private ArgumentCaptor<Iterable<LadderEntity>> savedLadders;

  private RoundEntity round;

  @BeforeEach
  void setUp() {
    round = RoundEntity.builder()
        .id(1L)
        .seasonId(1L)
        .number(1)
        .assholeLadderNumber(15)
        .basePointsRequirement(BigInteger.valueOf(1000))
        .percentageOfAdditionalAssholes(0.5f)
        .types(EnumSet.of(RoundType.DEFAULT))
        .build();
  }

  @Test
  void backfillLadders_preservesExistingAndPersistsFullLayout() {
    Set<LadderEntity> existing = new HashSet<>();
    IntStream.rangeClosed(1, 11).forEach(number -> existing.add(ladder(number,
        LadderType.DEFAULT).setId((long) number).setFirstReachedOn(OffsetDateTime.now())));
    var generated = new LadderGenerationService(List.of(new DefaultLadderGenerator()))
        .generateLaddersForRound(round);
    when(ladderRepository.findByRound(round.getId())).thenReturn(existing);
    when(ladderGenerationService.generateLaddersForRound(round)).thenReturn(generated);

    service.backfillLadders(round);

    verify(ladderRepository).saveAllAndFlush(savedLadders.capture());
    var saved = new ArrayList<LadderEntity>();
    savedLadders.getValue().forEach(saved::add);
    assertThat(saved).extracting(LadderEntity::getNumber)
        .containsExactlyElementsOf(IntStream.rangeClosed(1, 16).boxed().toList());
    existing.forEach(ladder -> {
      assertThat(saved.get(ladder.getNumber() - 1)).isSameAs(ladder);
      assertThat(ladder.getTypes()).containsExactly(LadderType.DEFAULT);
      assertThat(ladder.getBasePointsToPromote()).isEqualTo(BigInteger.TEN);
      assertThat(ladder.getFirstReachedOn()).isNotNull();
    });
    assertThat(saved.subList(11, 16)).containsExactlyElementsOf(generated.subList(11, 16));
    assertThat(saved.get(14).hasType(LadderType.ASSHOLE)).isTrue();
    assertThat(saved.getLast().hasType(LadderType.END)).isTrue();

    when(ladderRepository.findByRound(round.getId())).thenReturn(new HashSet<>(saved));
    service.backfillLadders(round);
    verify(ladderGenerationService).generateLaddersForRound(round);
    verify(ladderRepository).saveAllAndFlush(any());
  }

  @Test
  void backfillLadders_completeEndingDoesNotGenerateOrPersist() {
    when(ladderRepository.findByRound(round.getId())).thenReturn(Set.of(
        ladder(15, LadderType.ASSHOLE), ladder(16, LadderType.END)));

    service.backfillLadders(round);

    verifyNoInteractions(ladderGenerationService);
    verify(ladderRepository, never()).saveAllAndFlush(any());
  }

  @Test
  void backfillLadders_missingEndPreservesAssholeLadder() {
    LadderEntity asshole = ladder(15, LadderType.ASSHOLE).setId(15L);
    LadderEntity end = ladder(16, LadderType.END);
    when(ladderRepository.findByRound(round.getId())).thenReturn(Set.of(asshole));
    when(ladderGenerationService.generateLaddersForRound(round)).thenReturn(
        List.of(ladder(15, LadderType.ASSHOLE), end));

    service.backfillLadders(round);

    verify(ladderRepository).saveAllAndFlush(savedLadders.capture());
    assertThat(savedLadders.getValue()).containsExactly(asshole, end);
  }

  @Test
  void backfillLadders_endWithoutAssholeDoesNotCountAsComplete() {
    LadderEntity end = ladder(16, LadderType.END).setId(16L);
    LadderEntity asshole = ladder(15, LadderType.ASSHOLE);
    when(ladderRepository.findByRound(round.getId())).thenReturn(Set.of(end));
    when(ladderGenerationService.generateLaddersForRound(round)).thenReturn(
        List.of(asshole, ladder(16, LadderType.END)));

    service.backfillLadders(round);

    verify(ladderRepository).saveAllAndFlush(savedLadders.capture());
    assertThat(savedLadders.getValue()).containsExactly(asshole, end);
  }

  @Test
  void backfillLadders_emptyRoundPersistsGeneratedLayout() {
    when(ladderRepository.findByRound(round.getId())).thenReturn(Set.of());
    List<LadderEntity> generated = List.of(
        ladder(15, LadderType.ASSHOLE), ladder(16, LadderType.END));
    when(ladderGenerationService.generateLaddersForRound(round)).thenReturn(generated);

    service.backfillLadders(round);

    verify(ladderRepository).saveAllAndFlush(savedLadders.capture());
    assertThat(savedLadders.getValue()).containsExactlyElementsOf(generated);
  }

  @Test
  void backfillLadders_special100EndingDoesNotRequireAsshole() {
    round.setTypes(EnumSet.of(RoundType.SPECIAL_100));
    when(ladderRepository.findByRound(round.getId())).thenReturn(Set.of(
        ladder(100, LadderType.NO_AUTO), ladder(101, LadderType.END)));

    service.backfillLadders(round);

    verifyNoInteractions(ladderGenerationService);
    verify(ladderRepository, never()).saveAllAndFlush(any());
  }

  private LadderEntity ladder(int number, LadderType type) {
    return LadderEntity.builder()
        .roundId(round.getId())
        .number(number)
        .scaling(number)
        .basePointsToPromote(BigInteger.TEN)
        .types(EnumSet.of(type))
        .build();
  }
}
