package de.kaliburg.morefair.game.round.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationService;
import de.kaliburg.morefair.game.ladder.services.impl.LadderServiceImpl;
import de.kaliburg.morefair.game.ladder.services.repositories.LadderRepository;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import de.kaliburg.morefair.game.round.services.repositories.RoundRepository;
import de.kaliburg.morefair.game.round.services.utils.RoundUtilsService;
import de.kaliburg.morefair.game.season.model.SeasonEntity;
import de.kaliburg.morefair.game.season.services.SeasonService;
import de.kaliburg.morefair.game.season.services.impl.SeasonServiceImpl;
import de.kaliburg.morefair.game.season.services.repositories.SeasonRepository;
import java.math.BigInteger;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@DataJpaTest(showSql = false)
@ContextConfiguration(classes = RoundInitializationServiceTest.Config.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class RoundInitializationServiceTest {

  @Autowired
  private RoundInitializationService initializationService;
  @Autowired
  private RoundRepository roundRepository;
  @Autowired
  private LadderRepository ladderRepository;
  @Autowired
  private RoundService roundService;
  @Autowired
  private PlatformTransactionManager transactionManager;
  @MockBean
  private LadderGenerationService generator;
  @Autowired
  private SeasonService seasonService;
  @Autowired
  private SeasonRepository seasonRepository;
  @MockBean
  private WsUtils wsUtils;
  @MockBean
  private RoundUtilsService roundUtilsService;

  @BeforeEach
  void cleanDatabase() {
    ladderRepository.deleteAll();
    roundRepository.deleteAll();
    seasonRepository.deleteAll();
    reset(generator);
    ReflectionTestUtils.setField(roundService, "currentRoundId", null);
    ReflectionTestUtils.setField(seasonService, "currentSeason", null);
  }

  @Test
  void createsRoundAndCompleteLadderSet() {
    when(generator.generateLaddersForRound(any())).thenAnswer(invocation ->
        laddersFor(invocation.getArgument(0)));

    RoundEntity round = initializationService.create(newRound());

    assertThat(roundRepository.findById(round.getId())).isPresent();
    assertThat(ladderRepository.findByRound(round.getId()))
        .hasSize(2)
        .allSatisfy(ladder -> {
          assertThat(ladder.getId()).isNotNull();
          assertThat(ladder.getRoundId()).isEqualTo(round.getId());
        })
        .extracting(LadderEntity::getNumber).containsExactlyInAnyOrder(1, 2);
  }

  @Test
  void generationFailureRollsBackRoundAndAllowsRetry() {
    when(generator.generateLaddersForRound(any()))
        .thenThrow(new IllegalStateException("Generation failed"))
        .thenAnswer(invocation -> laddersFor(invocation.getArgument(0)));

    assertThatThrownBy(() -> initializationService.create(newRound()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Generation failed");
    assertThat(roundRepository.count()).isZero();
    assertThat(ladderRepository.count()).isZero();

    initializationService.create(newRound());

    assertThat(roundRepository.count()).isEqualTo(1);
    assertThat(ladderRepository.count()).isEqualTo(2);
  }

  @Test
  void persistenceFailureRollsBackRoundAndPreviouslyInsertedLadders() {
    when(generator.generateLaddersForRound(any())).thenAnswer(invocation -> {
      List<LadderEntity> ladders = laddersFor(invocation.getArgument(0));
      UUID duplicateUuid = UUID.randomUUID();
      ladders.forEach(ladder -> ladder.setUuid(duplicateUuid));
      return ladders;
    });

    assertThatThrownBy(() -> initializationService.create(newRound()))
        .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);

    assertThat(roundRepository.count()).isZero();
    assertThat(ladderRepository.count()).isZero();
  }

  @Test
  void incompleteGeneratedSetRollsBackRound() {
    when(generator.generateLaddersForRound(any())).thenReturn(List.of());

    assertThatThrownBy(() -> initializationService.create(newRound()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Incomplete ladder set");
    assertThat(roundRepository.count()).isZero();
  }

  @Test
  void currentRoundInitializationCommitsBeforePublishingToTransactionalCaller() {
    when(roundUtilsService.getRoundBasePointRequirementMultiplier(any())).thenReturn(1.d);
    when(roundUtilsService.determineAssholeLadder(anyInt(), any())).thenReturn(1);
    when(generator.generateLaddersForRound(any()))
        .thenAnswer(invocation -> laddersFor(invocation.getArgument(0)));

    new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
      roundService.getCurrentRound();
      status.setRollbackOnly();
    });

    RoundEntity round = roundService.getCurrentRound();
    assertThat(seasonRepository.findById(round.getSeasonId())).isPresent();
    assertThat(roundRepository.findById(round.getId())).isPresent();
    assertThat(ladderRepository.findByRound(round.getId())).hasSize(2);

    ReflectionTestUtils.setField(roundService, "currentRoundId", null);
    LadderServiceImpl reloadedService = new LadderServiceImpl(
        ladderRepository, roundService, initializationService);
    reloadedService.reloadLadders();

    assertThat(reloadedService.findCurrentLadderWithNumber(1)).isPresent();
    assertThat(reloadedService.findCurrentLadderWithNumber(2)).isPresent();
    verify(generator, times(1)).generateLaddersForRound(any());
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void seasonCreationCommitsIndependentlyOfCaller(boolean newest) {
    new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
      if (newest) {
        seasonService.findNewestSeason();
      } else {
        seasonService.getCurrentSeason();
      }
      status.setRollbackOnly();
    });

    assertThat(seasonRepository.count()).isEqualTo(1);
    ReflectionTestUtils.setField(seasonService, "currentSeason", null);
    assertThat(seasonService.getCurrentSeason().getNumber()).isEqualTo(1);
    assertThat(seasonRepository.count()).isEqualTo(1);
  }

  @Test
  void seasonTransitionCommitsIndependentlyOfCaller() {
    SeasonEntity older = seasonRepository.saveAndFlush(SeasonEntity.builder().number(1).build());
    SeasonEntity newer = seasonRepository.saveAndFlush(SeasonEntity.builder().number(2).build());

    new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
      assertThat(seasonService.findNewestSeason().getId()).isEqualTo(newer.getId());
      status.setRollbackOnly();
    });

    assertThat(seasonRepository.findById(older.getId()).orElseThrow().getClosedOn()).isNotNull();
    assertThat(seasonRepository.findById(newer.getId()).orElseThrow().getClosedOn()).isNull();
  }

  private RoundEntity newRound() {
    return RoundEntity.builder()
        .seasonId(1L)
        .number(1)
        .assholeLadderNumber(1)
        .basePointsRequirement(BigInteger.TEN)
        .percentageOfAdditionalAssholes(50.f)
        .types(EnumSet.of(RoundType.DEFAULT))
        .build();
  }

  private List<LadderEntity> laddersFor(RoundEntity round) {
    return List.of(
        LadderEntity.builder()
            .roundId(round.getId())
            .number(1)
            .scaling(1)
            .basePointsToPromote(BigInteger.TEN)
            .types(EnumSet.of(LadderType.ASSHOLE))
            .build(),
        LadderEntity.builder()
            .roundId(round.getId())
            .number(2)
            .scaling(2)
            .basePointsToPromote(BigInteger.TEN)
            .types(EnumSet.of(LadderType.END))
            .build());
  }

  @Configuration
  @EntityScan(basePackageClasses = {RoundEntity.class, LadderEntity.class, SeasonEntity.class})
  @EnableJpaRepositories(basePackageClasses = {
      RoundRepository.class, LadderRepository.class, SeasonRepository.class})
  @Import({RoundInitializationService.class, RoundService.class, FairConfig.class,
      SeasonServiceImpl.class})
  static class Config {
  }
}
