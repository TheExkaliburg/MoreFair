package de.kaliburg.morefair.game.ladder.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.LadderEventType;
import de.kaliburg.morefair.game.UpgradeUtils;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.ranker.services.utils.RankerUtilsService;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.UnlocksEntity;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.game.round.services.UnlocksService;
import de.kaliburg.morefair.game.season.model.AchievementsEntity;
import de.kaliburg.morefair.game.season.services.AchievementsService;
import de.kaliburg.morefair.game.vinegar.services.VinegarThrowService;
import de.kaliburg.morefair.moderation.events.model.UserEventMetadata;
import de.kaliburg.morefair.moderation.events.services.UserEventService;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LadderEventServiceTest {

  private static final UserEventMetadata METADATA = new UserEventMetadata(true, 120, 240);

  @Mock
  private AccountService accountService;
  @Mock
  private RankerService rankerService;
  @Mock
  private LadderService ladderService;
  @Mock
  private RoundService roundService;
  @Mock
  private RankerUtilsService rankerUtilsService;
  @Mock
  private UpgradeUtils upgradeUtils;
  @Mock
  private UserEventService userEventService;
  @Mock
  private WsUtils wsUtils;
  @Mock
  private UnlocksService unlocksService;
  @Mock
  private AchievementsService achievementsService;
  @Mock
  private VinegarThrowService vinegarThrowService;
  @InjectMocks
  private LadderEventServiceImpl service;

  private AccountEntity account;
  private RankerEntity ranker;
  private LadderEntity ladder;
  private RoundEntity round;

  @BeforeEach
  void setUp() {
    account = new AccountEntity();
    account.setId(7L);
    ranker = RankerEntity.builder().accountId(7L).ladderId(12L).rank(2).build();
    ladder = new LadderEntity();
    ladder.setId(12L).setNumber(1).setScaling(1).setRoundId(20L);
    round = new RoundEntity();
    round.setId(20L);

    when(roundService.getCurrentRound()).thenReturn(round);
    when(ladderService.findAllByRound(round)).thenReturn(List.of(ladder));
    when(ladderService.findCurrentLadderWithNumber(1)).thenReturn(Optional.of(ladder));
  }

  private void setUpActor() {
    when(accountService.findById(7L)).thenReturn(Optional.of(account));
    when(rankerService.findHighestActiveRankerOfAccount(account)).thenReturn(Optional.of(ranker));
  }

  @ParameterizedTest
  @EnumSource(value = LadderEventType.class, names = {
      "BUY_BIAS", "BUY_MULTI", "BUY_AUTO_PROMOTE", "PROMOTE"})
  void rejectedActionsDoNotRecordStatistics(LadderEventType type) throws Exception {
    setUpActor();
    if (type == LadderEventType.BUY_BIAS || type == LadderEventType.BUY_MULTI) {
      when(upgradeUtils.buyUpgradeCost(anyInt(), anyInt(), anySet())).thenReturn(BigInteger.TEN);
    }
    service.addEvent(1, new Event<>(type, 7L), METADATA);
    verifyNoInteractions(userEventService);

    service.handleEvents();

    verifyNoInteractions(userEventService);
    assertThat(ranker.isPromoted()).isFalse();
    assertThat(ranker.isAutoPromote()).isFalse();
  }

  @ParameterizedTest
  @EnumSource(value = LadderEventType.class, names = {"BUY_BIAS", "BUY_MULTI"})
  void upgradesRecordProcessingStateAfterValidationBeforeMutation(LadderEventType type)
      throws Exception {
    setUpActor();
    when(upgradeUtils.buyUpgradeCost(anyInt(), anyInt(), anySet())).thenReturn(BigInteger.TEN);
    service.addEvent(1, new Event<>(type, 7L), METADATA);
    verifyNoInteractions(userEventService);
    ranker.setPoints(BigInteger.valueOf(1100));
    ranker.setPower(BigInteger.valueOf(2200));
    doAnswer(invocation -> {
      assertThat(ranker.getPoints()).isEqualTo(BigInteger.valueOf(1100));
      assertThat(ranker.getPower()).isEqualTo(BigInteger.valueOf(2200));
      assertThat(ranker.getBias()).isZero();
      assertThat(ranker.getMultiplier()).isEqualTo(1);
      return null;
    }).when(userEventService).record(7L, type, METADATA);

    service.handleEvents();
    service.handleEvents();

    verify(userEventService).record(7L, type, METADATA);
    assertThat(ranker.getPoints()).isEqualTo(BigInteger.ZERO);
    if (type == LadderEventType.BUY_BIAS) {
      assertThat(ranker.getBias()).isEqualTo(1);
    } else {
      assertThat(ranker.getMultiplier()).isEqualTo(2);
      assertThat(ranker.getPower()).isEqualTo(BigInteger.ZERO);
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void autoPromotePurchasesRecordBeforeMutationIncludingFreeAuto(boolean free) throws Exception {
    setUpActor();
    ranker.setGrapes(BigInteger.valueOf(100));
    when(upgradeUtils.buyAutoPromoteCost(round, ladder, 2)).thenReturn(BigInteger.TEN);
    if (free) {
      ladder.getTypes().add(LadderType.FREE_AUTO);
    } else {
      when(rankerUtilsService.canBuyAutoPromote(ranker)).thenReturn(true);
    }
    doAnswer(invocation -> {
      assertThat(ranker.isAutoPromote()).isFalse();
      assertThat(ranker.getGrapes()).isEqualTo(BigInteger.valueOf(100));
      return null;
    }).when(userEventService).record(7L, LadderEventType.BUY_AUTO_PROMOTE, METADATA);
    service.addEvent(1, new Event<>(LadderEventType.BUY_AUTO_PROMOTE, 7L), METADATA);

    service.handleEvents();

    verify(userEventService).record(7L, LadderEventType.BUY_AUTO_PROMOTE, METADATA);
    assertThat(ranker.isAutoPromote()).isTrue();
    assertThat(ranker.getGrapes()).isEqualTo(BigInteger.valueOf(free ? 100 : 90));
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void promotionRecordsManualAndAutomaticActionsBeforeCreatingTheNextRanker(boolean userAction)
      throws Exception {
    setUpActor();
    RankerEntity nextRanker = RankerEntity.builder().accountId(7L).ladderId(13L).rank(1).build();
    LadderEntity nextLadder = new LadderEntity();
    nextLadder.setId(13L).setNumber(2);
    when(rankerUtilsService.canPromote(ranker)).thenReturn(true);
    when(rankerService.createRankerOnLadder(account, 2)).thenReturn(Optional.of(nextRanker));
    when(ladderService.findLadderById(13L)).thenReturn(Optional.of(nextLadder));
    when(rankerService.findAllByLadderId(13L)).thenReturn(List.of(nextRanker));
    when(unlocksService.findOrCreateByAccountInCurrentRound(7L)).thenReturn(new UnlocksEntity());
    when(achievementsService.findOrCreateByAccountInCurrentSeason(7L))
        .thenReturn(new AchievementsEntity());
    when(rankerUtilsService.getWinningGrapes(ladder)).thenReturn(BigInteger.ONE);
    when(accountService.save(account)).thenReturn(account);
    UserEventMetadata metadata = userAction ? METADATA : null;
    doAnswer(invocation -> {
      assertThat(ranker.isPromoted()).isFalse();
      verify(rankerService, never()).createRankerOnLadder(any(), anyInt());
      return null;
    }).when(userEventService).record(7L, LadderEventType.PROMOTE, metadata);
    if (userAction) {
      service.addEvent(1, new Event<>(LadderEventType.PROMOTE, 7L), METADATA);
    } else {
      service.addEvent(1, new Event<>(LadderEventType.PROMOTE, 7L));
    }

    service.handleEvents();

    assertThat(ranker.isPromoted()).isTrue();
    verify(userEventService).record(7L, LadderEventType.PROMOTE, metadata);
  }

  @Test
  void vinegarCarriesMetadataToItsOwnEligibilityChecks() throws Exception {
    Event<LadderEventType> event = new Event<>(LadderEventType.THROW_VINEGAR, 7L, 50);
    service.addEvent(1, event, METADATA);
    when(vinegarThrowService.throwVinegar(event, METADATA)).thenReturn(Optional.empty());

    service.handleEvents();

    verify(vinegarThrowService).throwVinegar(event, METADATA);
    verifyNoInteractions(userEventService);
  }
}
