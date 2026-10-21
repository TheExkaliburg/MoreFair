package de.kaliburg.morefair.game.ranker.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.game.UpgradeUtils;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.model.generation.LadderGenerationService;
import de.kaliburg.morefair.game.ladder.services.impl.LadderServiceImpl;
import de.kaliburg.morefair.game.ladder.services.repositories.LadderRepository;
import de.kaliburg.morefair.game.ladder.services.utils.LadderUtilsServiceImpl;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.services.repositories.RankerRepository;
import de.kaliburg.morefair.game.ranker.services.utils.RankerUtilsServiceImpl;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import de.kaliburg.morefair.game.round.services.RoundInitializationService;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.game.round.services.repositories.RoundRepository;
import de.kaliburg.morefair.game.season.model.AchievementsEntity;
import de.kaliburg.morefair.game.season.services.AchievementsService;
import java.math.BigInteger;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest(showSql = false)
@ContextConfiguration(classes = RankerServiceImplTest.Config.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class RankerServiceImplTest {

  @Autowired
  private RoundRepository roundRepository;
  @Autowired
  private LadderRepository ladderRepository;
  @Autowired
  private RankerRepository rankerRepository;
  private LadderServiceImpl ladderService;
  private RankerServiceImpl rankerService;
  private RoundService roundService;
  private RoundEntity round;
  private AccountEntity account;

  @BeforeEach
  void setup() {
    rankerRepository.deleteAll();
    ladderRepository.deleteAll();
    roundRepository.deleteAll();
    round = roundRepository.saveAndFlush(RoundEntity.builder()
        .seasonId(1L)
        .number(1)
        .assholeLadderNumber(1)
        .basePointsRequirement(BigInteger.TEN)
        .percentageOfAdditionalAssholes(50.f)
        .types(EnumSet.of(RoundType.DEFAULT))
        .build());
    roundService = mock(RoundService.class);
    when(roundService.getCurrentRound()).thenReturn(round);
    LadderGenerationService generator = new LadderGenerationService(List.of());
    ladderService = new LadderServiceImpl(ladderRepository, roundService,
        new RoundInitializationService(roundRepository, ladderRepository, generator));
    AchievementsService achievementsService = mock(AchievementsService.class);
    when(achievementsService.findOrCreateByAccountInCurrentSeason(anyLong()))
        .thenAnswer(invocation -> AchievementsEntity.builder()
            .accountId(invocation.getArgument(0))
            .seasonId(1L)
            .build());
    rankerService = new RankerServiceImpl(rankerRepository, ladderService, generator, roundService,
        achievementsService, mock(WsUtils.class), new FairConfig());
    account = new AccountEntity().setId(1L);
    saveLadder(1, LadderType.DEFAULT);
  }

  @Test
  void missingDestinationPlacesPlayerOnPersistedVoidLadder() {
    RankerEntity ranker = rankerService.createRankerOnLadder(account, 2).orElseThrow();

    LadderEntity destination = ladderService.findLadderById(ranker.getLadderId()).orElseThrow();
    assertThat(destination.getTypes()).containsExactly(LadderType.VOID);
    assertThat(destination.getNumber()).isEqualTo(2);
    assertThat(destination.getRoundId()).isEqualTo(round.getId());
    assertThat(ladderRepository.findById(destination.getId()).orElseThrow().getTypes())
        .containsExactly(LadderType.VOID);
    assertThat(rankerRepository.findById(ranker.getId()).orElseThrow().getLadderId())
        .isEqualTo(destination.getId());
    assertThat(rankerService.findHighestActiveRankerOfAccount(account)).contains(ranker);

    ladderService.reloadLadders();

    assertThat(ladderService.findCurrentLadderWithNumber(2).orElseThrow().getId())
        .isEqualTo(destination.getId());
  }

  @Test
  void existingDestinationIsUsedWithoutCreatingVoid() {
    LadderEntity existing = saveLadder(2, LadderType.SMALL);

    RankerEntity ranker = rankerService.createRankerOnLadder(account, 2).orElseThrow();

    assertThat(ranker.getLadderId()).isEqualTo(existing.getId());
    assertThat(ladderRepository.findById(existing.getId()).orElseThrow().getTypes())
        .containsExactly(LadderType.SMALL);
    assertThat(ladderRepository.count()).isEqualTo(2);
  }

  @Test
  void playersWithTheSameMissingDestinationShareVoidLadder() {
    RankerEntity first = rankerService.createRankerOnLadder(account, 2).orElseThrow();
    AccountEntity otherAccount = new AccountEntity().setId(2L);

    RankerEntity second = rankerService.createRankerOnLadder(otherAccount, 2).orElseThrow();

    assertThat(second.getLadderId()).isEqualTo(first.getLadderId());
    assertThat(ladderService.findLadderById(second.getLadderId()).orElseThrow().getTypes())
        .containsExactly(LadderType.VOID);
    assertThat(rankerRepository.findByLadderId(first.getLadderId())).hasSize(2);
    assertThat(ladderRepository.count()).isEqualTo(2);
  }

  @ParameterizedTest
  @EnumSource(value = LadderType.class, names = {"DEFAULT", "END", "VOID"})
  void onlyNonterminalLaddersAllowOtherwiseEligiblePlayersToPromote(LadderType type) {
    round.setTypes(EnumSet.of(RoundType.RACE));
    when(roundService.findById(round.getId())).thenReturn(Optional.of(round));
    LadderEntity ladder = saveLadder(2, type);
    RankerEntity ranker = rankerService.createRankerOnLadder(account, 2).orElseThrow();
    ranker.setPoints(BigInteger.valueOf(1_000_000));
    ranker.setAutoPromote(true);
    FairConfig config = new FairConfig();
    LadderUtilsServiceImpl ladderUtils =
        new LadderUtilsServiceImpl(config, rankerService, roundService);
    RankerUtilsServiceImpl rankerUtils = new RankerUtilsServiceImpl(
        ladderService, rankerService, new UpgradeUtils(config, ladderUtils), roundService,
        ladderUtils, config);

    assertThat(rankerUtils.canPromote(ranker)).isEqualTo(type == LadderType.DEFAULT);
    assertThat(ladder.hasType(LadderType.END)).isEqualTo(type != LadderType.DEFAULT);
    assertThat(ladder.hasType(LadderType.VOID)).isEqualTo(type == LadderType.VOID);
    assertThat(ladder.getTypes().contains(LadderType.END)).isEqualTo(type == LadderType.END);
  }

  private LadderEntity saveLadder(int number, LadderType type) {
    return ladderRepository.saveAndFlush(LadderEntity.builder()
        .roundId(round.getId())
        .number(number)
        .scaling(number)
        .basePointsToPromote(BigInteger.TEN)
        .types(EnumSet.of(type))
        .build());
  }

  @Configuration
  @EntityScan(basePackageClasses = {RoundEntity.class, LadderEntity.class, RankerEntity.class})
  @EnableJpaRepositories(basePackageClasses = {
      RoundRepository.class, LadderRepository.class, RankerRepository.class})
  static class Config {
  }
}
