package de.kaliburg.morefair.game.vinegar.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.data.VinegarData.VinegarSuccessType;
import de.kaliburg.morefair.events.types.LadderEventType;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.LadderType;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.ranker.services.utils.RankerUtilsService;
import de.kaliburg.morefair.game.season.model.AchievementsEntity;
import de.kaliburg.morefair.game.season.services.AchievementsService;
import de.kaliburg.morefair.game.vinegar.model.VinegarThrowEntity;
import de.kaliburg.morefair.game.vinegar.services.repositories.VinegarThrowRepository;
import de.kaliburg.morefair.moderation.events.model.UserEventMetadata;
import de.kaliburg.morefair.moderation.events.services.UserEventService;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VinegarThrowServiceTest {

  private static final UserEventMetadata METADATA = new UserEventMetadata(true, 120, 240);
  @Mock
  private AccountService accountService;
  @Mock
  private RankerService rankerService;
  @Mock
  private LadderService ladderService;
  @Mock
  private RankerUtilsService rankerUtilsService;
  @Mock
  private UserEventService userEventService;
  @Mock
  private VinegarThrowRepository repository;
  @Mock
  private AchievementsService achievementsService;
  @InjectMocks
  private VinegarThrowServiceImpl service;

  private RankerEntity thrower;
  private RankerEntity target;
  private LadderEntity ladder;

  @BeforeEach
  void setUp() {
    AccountEntity throwerAccount = new AccountEntity();
    throwerAccount.setId(7L);
    AccountEntity targetAccount = new AccountEntity();
    targetAccount.setId(8L);
    thrower = RankerEntity.builder().accountId(7L).ladderId(12L).rank(2)
        .vinegar(BigInteger.valueOf(100)).build();
    target = RankerEntity.builder().accountId(8L).ladderId(12L).rank(1)
        .vinegar(BigInteger.valueOf(500)).build();
    ladder = new LadderEntity();
    ladder.setId(12L).setNumber(1);
    when(accountService.findById(7L)).thenReturn(Optional.of(throwerAccount));
    when(accountService.findById(8L)).thenReturn(Optional.of(targetAccount));
    when(rankerService.findHighestActiveRankerOfAccount(throwerAccount))
        .thenReturn(Optional.of(thrower));
    when(ladderService.findLadderById(12L)).thenReturn(Optional.of(ladder));
    when(rankerService.findAllByLadderId(12L)).thenReturn(List.of(target, thrower));
  }

  @ParameterizedTest
  @ValueSource(strings = {"AUTO_PROMOTE", "FREE_AUTO", "INELIGIBLE"})
  void rejectedThrowsDoNotRecordStatistics(String reason) {
    if (reason.equals("AUTO_PROMOTE")) {
      target.setAutoPromote(true);
    } else if (reason.equals("FREE_AUTO")) {
      ladder.getTypes().add(LadderType.FREE_AUTO);
    }

    var result = service.throwVinegar(new Event<>(LadderEventType.THROW_VINEGAR, 7L, 100), METADATA);

    assertThat(result).isEmpty();
    verifyNoInteractions(userEventService, repository, achievementsService);
    assertThat(thrower.getVinegar()).isEqualTo(BigInteger.valueOf(100));
    assertThat(target.getVinegar()).isEqualTo(BigInteger.valueOf(500));
  }

  @Test
  void eligibleButDefendedThrowRecordsBeforeChangingResources() {
    when(rankerUtilsService.canThrowVinegarAt(thrower, target, 100)).thenReturn(true);
    when(achievementsService.findOrCreateByAccountInCurrentSeason(7L))
        .thenReturn(new AchievementsEntity());
    when(achievementsService.findOrCreateByAccountInCurrentSeason(8L))
        .thenReturn(new AchievementsEntity());
    when(repository.save(any(VinegarThrowEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    doAnswer(invocation -> {
      assertThat(thrower.getVinegar()).isEqualTo(BigInteger.valueOf(100));
      assertThat(target.getVinegar()).isEqualTo(BigInteger.valueOf(500));
      return null;
    }).when(userEventService).recordAsync(7L, LadderEventType.THROW_VINEGAR, METADATA);

    var result = service.throwVinegar(new Event<>(LadderEventType.THROW_VINEGAR, 7L, 100), METADATA);

    assertThat(result).isPresent();
    assertThat(result.get().getSuccessType()).isEqualTo(VinegarSuccessType.DEFENDED);
    verify(userEventService).recordAsync(7L, LadderEventType.THROW_VINEGAR, METADATA);
    assertThat(thrower.getVinegar()).isEqualTo(BigInteger.ZERO);
    assertThat(target.getVinegar()).isEqualTo(BigInteger.valueOf(400));
  }
}
