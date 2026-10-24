package de.kaliburg.morefair.moderation.events.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.events.types.LadderEventType;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.moderation.events.model.UserEventEntity;
import de.kaliburg.morefair.moderation.events.model.UserEventMetadata;
import de.kaliburg.morefair.moderation.events.model.dto.UserEventRequest;
import de.kaliburg.morefair.moderation.events.services.impl.UserEventServiceImpl;
import de.kaliburg.morefair.moderation.events.services.repositories.UserEventRepository;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserEventServiceTest {

  @Mock
  private UserEventRepository repository;
  @Mock
  private AccountService accountService;
  @Mock
  private RankerService rankerService;
  @Mock
  private LadderService ladderService;
  @Mock
  private RoundService roundService;
  @InjectMocks
  private UserEventServiceImpl service;

  @ParameterizedTest
  @CsvSource({
      "BUY_BIAS, true", "BUY_MULTI, true", "BUY_AUTO_PROMOTE, true",
      "PROMOTE, true", "THROW_VINEGAR, true", "PROMOTE, false"})
  void record_copiesProcessingTimeStateWithoutRetainingMutableEntities(LadderEventType type,
      boolean userAction) {
    AccountEntity account = new AccountEntity();
    account.setId(7L);
    BigInteger points = BigInteger.TEN.pow(999).add(BigInteger.ONE);
    RankerEntity ranker = RankerEntity.builder()
        .accountId(7L).ladderId(12L).rank(2).points(points).bias(4).build();
    RankerEntity target = RankerEntity.builder()
        .accountId(8L).ladderId(12L).rank(1).build();
    LadderEntity ladder = LadderEntity.builder().id(12L).roundId(20L).number(2).scaling(3)
        .basePointsToPromote(BigInteger.ONE).build();
    LadderEntity firstLadder = LadderEntity.builder().id(11L).roundId(20L).number(1).scaling(1)
        .basePointsToPromote(BigInteger.ONE).build();
    RoundEntity round = RoundEntity.builder().id(20L).seasonId(3L).number(5)
        .assholeLadderNumber(100).basePointsRequirement(BigInteger.ONE)
        .percentageOfAdditionalAssholes(0F).build();
    UserEventRequest request = request();

    when(accountService.findById(7L)).thenReturn(Optional.of(account));
    when(rankerService.findHighestActiveRankerOfAccount(account)).thenReturn(Optional.of(ranker));
    when(roundService.getCurrentRound()).thenReturn(round);
    when(ladderService.findLadderById(12L)).thenReturn(Optional.of(ladder));
    when(ladderService.findCurrentLadderWithNumber(1)).thenReturn(Optional.of(firstLadder));
    when(rankerService.findAllByLadderId(12L)).thenReturn(List.of(target, ranker));
    when(rankerService.findAllByLadderId(11L)).thenReturn(List.of());
    when(repository.save(any(UserEventEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var recorded = service.record(7L, type, userAction ? new UserEventMetadata(request) : null);
    ranker.setPoints(BigInteger.ZERO);
    ranker.setBias(5);
    ladder.setScaling(9);
    round.setNumber(6);
    request.setScreenX(999);

    assertThat(recorded.getCreatedOn()).isNotNull();
    if (userAction) {
      assertThat(recorded.getMetadataVersion()).isEqualTo(1);
      assertThat(recorded.getMetadata().screenX()).isEqualTo(120);
    } else {
      assertThat(recorded.getMetadataVersion()).isNull();
      assertThat(recorded.getMetadata()).isNull();
    }
    assertThat(recorded.getSnapshotVersion()).isEqualTo(1);
    var snapshot = recorded.getSnapshot();
    assertThat(snapshot.ranker().points()).isEqualTo(points.toString());
    assertThat(snapshot.ranker().bias()).isEqualTo(4);
    assertThat(snapshot.ladder().scaling()).isEqualTo(3);
    assertThat(snapshot.ladder().rankers()).isEqualTo(2);
    assertThat(snapshot.ladder().activeRankers()).isEqualTo(2);
    assertThat(snapshot.round().id()).isEqualTo(20L);
    assertThat(snapshot.round().seasonId()).isEqualTo(3L);
    assertThat(snapshot.round().number()).isEqualTo(5);
    assertThat(snapshot.round().firstLadder().number()).isEqualTo(1);
    if (type == LadderEventType.THROW_VINEGAR) {
      assertThat(snapshot.target().account()).isEqualTo(8L);
      assertThat(snapshot.second().account()).isEqualTo(7L);
    } else {
      assertThat(snapshot.target()).isNull();
      assertThat(snapshot.second()).isNull();
    }
  }

  @Test
  void record_withoutAnActiveRanker_preservesMetadataWithoutInventingASnapshot() {
    AccountEntity account = new AccountEntity();
    account.setId(7L);
    when(accountService.findById(7L)).thenReturn(Optional.of(account));
    when(rankerService.findHighestActiveRankerOfAccount(account)).thenReturn(Optional.empty());
    when(repository.save(any(UserEventEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var recorded = service.record(7L, LadderEventType.BUY_AUTO_PROMOTE,
        new UserEventMetadata(request()));

    assertThat(recorded.getMetadata().isTrusted()).isTrue();
    assertThat(recorded.getMetadata().screenY()).isEqualTo(240);
    assertThat(recorded.getSnapshot()).isNull();
    assertThat(recorded.getSnapshotVersion()).isNull();
  }

  private static UserEventRequest request() {
    UserEventRequest request = new UserEventRequest();
    request.setIsTrusted(true);
    request.setScreenX(120);
    request.setScreenY(240);
    return request;
  }
}
