package de.kaliburg.morefair.api;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.api.websockets.messages.WsMessage;
import de.kaliburg.morefair.events.types.LadderEventType;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderEventService;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ladder.services.LadderTickService;
import de.kaliburg.morefair.game.ladder.services.mapper.LadderMapper;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.game.round.services.utils.RoundUtilsService;
import de.kaliburg.morefair.moderation.events.model.UserEventMetadata;
import de.kaliburg.morefair.moderation.events.model.dto.UserEventRequest;
import de.kaliburg.morefair.security.UserDetailsWithUuid;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class LadderControllerTest {

  @Mock
  private AccountService accountService;
  @Mock
  private RoundService roundService;
  @Mock
  private LadderService ladderService;
  @Mock
  private LadderTickService tickService;
  @Mock
  private LadderEventService eventService;
  @Mock
  private RoundUtilsService roundUtilsService;
  @Mock
  private LadderMapper ladderMapper;
  @Mock
  private RankerService rankerService;
  @Mock
  private FairConfig fairConfig;
  @InjectMocks
  private LadderController controller;

  @ParameterizedTest
  @EnumSource(value = LadderEventType.class, names = {
      "BUY_BIAS", "BUY_MULTI", "BUY_AUTO_PROMOTE", "PROMOTE", "THROW_VINEGAR"})
  void queuesAnImmutableCopyOfInteractionMetadata(LadderEventType type) {
    AccountEntity account = new AccountEntity();
    account.setId(7L);
    RankerEntity ranker = RankerEntity.builder().accountId(7L).ladderId(12L).rank(2).build();
    LadderEntity ladder = new LadderEntity();
    ladder.setId(12L).setNumber(2);
    Authentication authentication = mock(Authentication.class);
    UserDetailsWithUuid principal = mock(UserDetailsWithUuid.class);
    WsMessage message = new WsMessage();
    UserEventRequest request = new UserEventRequest();
    request.setIsTrusted(true);
    request.setScreenX(120);
    request.setScreenY(240);
    message.setEvent(request);

    when(authentication.getPrincipal()).thenReturn(principal);
    when(principal.getUuid()).thenReturn(account.getUuid());
    when(accountService.findByUuid(account.getUuid())).thenReturn(Optional.of(account));
    when(rankerService.findHighestActiveRankerOfAccount(account)).thenReturn(Optional.of(ranker));
    when(ladderService.findLadderById(12L)).thenReturn(Optional.of(ladder));

    switch (type) {
      case BUY_BIAS -> controller.buyBias(null, authentication, message);
      case BUY_MULTI -> controller.buyMulti(null, authentication, message);
      case BUY_AUTO_PROMOTE -> controller.buyAutoPromote(null, authentication, message);
      case PROMOTE -> controller.promote(null, authentication, message);
      case THROW_VINEGAR -> controller.throwVinegar(null, authentication, message);
      default -> throw new IllegalArgumentException("Unexpected test event: " + type);
    }

    request.setScreenX(999);
    verify(eventService).addEvent(eq(2),
        argThat(event -> event.getEventType() == type && event.getAccountId().equals(7L)),
        eq(new UserEventMetadata(true, 120, 240)));
    verifyNoMoreInteractions(eventService);
  }
}
