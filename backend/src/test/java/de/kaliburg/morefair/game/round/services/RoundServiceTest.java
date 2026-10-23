package de.kaliburg.morefair.game.round.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.type.RoundType;
import de.kaliburg.morefair.game.round.services.repositories.RoundRepository;
import de.kaliburg.morefair.game.round.services.utils.RoundUtilsService;
import de.kaliburg.morefair.game.season.model.SeasonEntity;
import de.kaliburg.morefair.game.season.services.SeasonService;
import java.math.BigInteger;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoundServiceTest {

  @Mock
  private RoundRepository roundRepository;
  @Mock
  private SeasonService seasonService;
  @Mock
  private WsUtils wsUtils;
  @Mock
  private RoundUtilsService roundUtilsService;
  @Mock
  private FairConfig fairConfig;
  @Mock
  private RoundInitializationService roundInitializationService;
  @Mock
  private SeasonEntity season;
  @InjectMocks
  private RoundService service;

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
    when(seasonService.getCurrentSeason()).thenReturn(season);
    when(season.getId()).thenReturn(1L);
    when(roundRepository.findNewestRoundOfSeason(1L)).thenReturn(Optional.of(round));
  }

  @Test
  void getCurrentRound_backfillsBeforeCachingExistingRoundAndOnlyOnce() {
    when(roundRepository.findById(round.getId())).thenReturn(Optional.of(round));

    assertThat(service.getCurrentRound()).isSameAs(round);
    assertThat(service.getCurrentRound()).isSameAs(round);

    var order = inOrder(roundInitializationService, roundRepository);
    order.verify(roundInitializationService).backfillLadders(round);
    order.verify(roundRepository).findById(round.getId());
    verify(roundInitializationService).backfillLadders(round);
  }

  @Test
  void getCurrentRound_failedBackfillPropagatesAndDoesNotMarkRoundInitialized() {
    doThrow(new IllegalStateException("Backfill failed"))
        .when(roundInitializationService).backfillLadders(round);

    assertThatThrownBy(service::getCurrentRound)
        .isInstanceOf(IllegalStateException.class).hasMessage("Backfill failed");
    assertThatThrownBy(service::getCurrentRound)
        .isInstanceOf(IllegalStateException.class).hasMessage("Backfill failed");

    verify(roundInitializationService, times(2)).backfillLadders(round);
  }
}
