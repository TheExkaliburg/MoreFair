package de.kaliburg.morefair.game.ladder.services.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.chat.services.ChatService;
import de.kaliburg.morefair.chat.services.MessageService;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.data.VinegarData.VinegarSuccessType;
import de.kaliburg.morefair.events.types.LadderEventType;
import de.kaliburg.morefair.game.UpgradeUtils;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.ranker.services.utils.RankerUtilsService;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.game.round.services.UnlocksService;
import de.kaliburg.morefair.game.round.services.utils.UnlocksUtilsService;
import de.kaliburg.morefair.game.season.services.AchievementsService;
import de.kaliburg.morefair.game.vinegar.model.VinegarThrowEntity;
import de.kaliburg.morefair.game.vinegar.model.dto.ThrowRecordResponse;
import de.kaliburg.morefair.game.vinegar.services.VinegarThrowService;
import de.kaliburg.morefair.game.vinegar.services.mapper.VinegarThrowMapper;
import de.kaliburg.morefair.statistics.services.StatisticsService;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LadderEventServiceImplTest {

  @Mock
  private AccountService accountService;
  @Mock
  private ChatService chatService;
  @Mock
  private MessageService messageService;
  @Mock
  private RankerService rankerService;
  @Mock
  private LadderService ladderService;
  @Mock
  private RankerUtilsService rankerUtilsService;
  @Mock
  private RoundService roundService;
  @Mock
  private UnlocksService unlocksService;
  @Mock
  private UnlocksUtilsService unlocksUtilsService;
  @Mock
  private AchievementsService achievementsService;
  @Mock
  private VinegarThrowService vinegarThrowService;
  @Mock
  private VinegarThrowMapper vinegarThrowMapper;
  @Mock
  private StatisticsService statisticsService;
  @Mock
  private WsUtils wsUtils;
  @Mock
  private UpgradeUtils upgradeUtils;
  @Mock
  private FairConfig fairConfig;

  private LadderEventServiceImpl service;

  @BeforeEach
  void setUp() {
    service = spy(new LadderEventServiceImpl(
        accountService,
        chatService,
        messageService,
        rankerService,
        ladderService,
        rankerUtilsService,
        roundService,
        unlocksService,
        unlocksUtilsService,
        achievementsService,
        vinegarThrowService,
        vinegarThrowMapper,
        statisticsService,
        wsUtils,
        upgradeUtils,
        fairConfig,
        new Gson()
    ));
  }

  @Test
  void shouldCallRemoveMultiOnlyOnceForSameTargetAcrossSuccessfulThrowsInOneTick() {
    long targetAccountId = 42L;
    long firstThrowerAccountId = 7L;
    long secondThrowerAccountId = 8L;
    Set<Long> dethronedTargetAccountIds = new HashSet<>();
    Event<LadderEventType> firstThrowEvent = new Event<>(LadderEventType.THROW_VINEGAR,
        firstThrowerAccountId);
    Event<LadderEventType> secondThrowEvent = new Event<>(LadderEventType.THROW_VINEGAR,
        secondThrowerAccountId);
    LadderEntity ladder = new LadderEntity()
        .setId(1L)
        .setNumber(1)
        .setRoundId(1L)
        .setBasePointsToPromote(BigInteger.ONE)
        .setScaling(1);

    VinegarThrowEntity successfulThrow = new VinegarThrowEntity(
        null,
        UUID.randomUUID(),
        OffsetDateTime.now(ZoneOffset.UTC),
        firstThrowerAccountId,
        targetAccountId,
        1L,
        BigInteger.ONE,
        100,
        BigInteger.ZERO,
        BigInteger.ZERO,
        VinegarSuccessType.SUCCESS,
        0,
        0
    );

    AccountEntity account = new AccountEntity().setUuid(UUID.randomUUID());
    RankerEntity topRanker = RankerEntity.builder()
        .ladderId(ladder.getId())
        .accountId(targetAccountId)
        .rank(1)
        .build();
    ThrowRecordResponse throwRecordResponse = new ThrowRecordResponse(
        firstThrowerAccountId,
        targetAccountId,
        0,
        1,
        1,
        "1",
        100,
        VinegarSuccessType.SUCCESS
    );

    when(vinegarThrowService.throwVinegar(any())).thenReturn(Optional.of(successfulThrow));
    when(vinegarThrowMapper.mapVinegarThrowToThrowRecord(successfulThrow)).thenReturn(throwRecordResponse);
    when(accountService.findById(any())).thenReturn(Optional.of(account));
    when(rankerService.findAllByLadderId(ladder.getId())).thenReturn(List.of(topRanker), List.of(topRanker));
    doNothing().when(service).removeMulti(any(), eq(ladder));

    assertTrue(service.throwVinegar(firstThrowEvent, ladder, dethronedTargetAccountIds));
    assertFalse(service.throwVinegar(secondThrowEvent, ladder, dethronedTargetAccountIds));

    verify(service, times(1)).removeMulti(any(), eq(ladder));
    verify(vinegarThrowService, times(1)).throwVinegar(any());
  }

  @Test
  void shouldCallRemoveMultiTwiceForSameTargetAcrossDifferentTickSets() {
    long firstThrowerAccountId = 7L;
    long secondThrowerAccountId = 8L;
    long targetAccountId = 42L;
    Set<Long> firstTickDethronedTargetIds = new HashSet<>();
    Set<Long> secondTickDethronedTargetIds = new HashSet<>();
    Event<LadderEventType> firstThrowEvent = new Event<>(LadderEventType.THROW_VINEGAR,
        firstThrowerAccountId);
    Event<LadderEventType> secondThrowEvent = new Event<>(LadderEventType.THROW_VINEGAR,
        secondThrowerAccountId);
    LadderEntity ladder = new LadderEntity()
        .setId(1L)
        .setNumber(1)
        .setRoundId(1L)
        .setBasePointsToPromote(BigInteger.ONE)
        .setScaling(1);

    VinegarThrowEntity firstTargetThrow = new VinegarThrowEntity(
        null,
        UUID.randomUUID(),
        OffsetDateTime.now(ZoneOffset.UTC),
        firstThrowerAccountId,
        targetAccountId,
        1L,
        BigInteger.ONE,
        100,
        BigInteger.ZERO,
        BigInteger.ZERO,
        VinegarSuccessType.SUCCESS,
        0,
        0
    );

    VinegarThrowEntity secondTargetThrow = new VinegarThrowEntity(
        null,
        UUID.randomUUID(),
        OffsetDateTime.now(ZoneOffset.UTC),
        secondThrowerAccountId,
        targetAccountId,
        1L,
        BigInteger.ONE,
        100,
        BigInteger.ZERO,
        BigInteger.ZERO,
        VinegarSuccessType.SUCCESS,
        0,
        0
    );

    AccountEntity account = new AccountEntity().setUuid(UUID.randomUUID());
    RankerEntity topRanker = RankerEntity.builder()
        .ladderId(ladder.getId())
        .accountId(targetAccountId)
        .rank(1)
        .build();
    ThrowRecordResponse firstThrowRecordResponse = new ThrowRecordResponse(
        firstThrowerAccountId,
        targetAccountId,
        0,
        1,
        1,
        "1",
        100,
        VinegarSuccessType.SUCCESS
    );
    ThrowRecordResponse secondThrowRecordResponse = new ThrowRecordResponse(
        secondThrowerAccountId,
        targetAccountId,
        0,
        1,
        1,
        "1",
        100,
        VinegarSuccessType.SUCCESS
    );

    when(vinegarThrowService.throwVinegar(any())).thenReturn(Optional.of(firstTargetThrow),
        Optional.of(secondTargetThrow));
    when(vinegarThrowMapper.mapVinegarThrowToThrowRecord(firstTargetThrow)).thenReturn(
        firstThrowRecordResponse);
    when(vinegarThrowMapper.mapVinegarThrowToThrowRecord(secondTargetThrow)).thenReturn(
        secondThrowRecordResponse);
    when(accountService.findById(any())).thenReturn(Optional.of(account));
    when(rankerService.findAllByLadderId(ladder.getId())).thenReturn(
        List.of(topRanker),
        List.of(topRanker));
    doNothing().when(service).removeMulti(any(), eq(ladder));

    assertTrue(service.throwVinegar(firstThrowEvent, ladder, firstTickDethronedTargetIds));
    assertTrue(service.throwVinegar(secondThrowEvent, ladder, secondTickDethronedTargetIds));

    verify(service, times(2)).removeMulti(any(), eq(ladder));
  }
}
