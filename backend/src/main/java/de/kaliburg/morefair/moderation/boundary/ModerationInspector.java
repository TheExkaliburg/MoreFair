package de.kaliburg.morefair.moderation.boundary;


import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.chat.services.MessageService;
import de.kaliburg.morefair.moderation.events.services.NameChangeService;
import de.kaliburg.morefair.moderation.events.services.UserEventService;
import de.kaliburg.morefair.moderation.model.dto.InspectorUserOverview;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Log4j2
@RequestMapping("/api/moderation/inspector/{accountId}")
@RequiredArgsConstructor
public class ModerationInspector {

  private final AccountService accountService;
  private final MessageService messageService;
  private final NameChangeService nameChangeService;
  private final UserEventService userEventService;

  @GetMapping
  public ResponseEntity<InspectorUserOverview> getInspectorUserOverview(
      @PathVariable Long accountId) {

    AccountEntity accountEntity = accountService.findById(accountId).orElseThrow();

    var result = InspectorUserOverview.builder()
        .displayName(accountEntity.getDisplayName())
        .messageLogCount(messageService.countAllByAccountId(accountEntity.getId()))
        .eventLogCount(userEventService.countByAccountId(accountEntity.getId()))
        .auditLogCount(null)
        .nameHistoryCount(nameChangeService.countAllByAccountId(accountEntity.getId()))
        .altAccountsCount(accountService.countAllByLastIp(accountEntity.getLastIp()))
        .build();

    return ResponseEntity.ok(result);
  }


}
