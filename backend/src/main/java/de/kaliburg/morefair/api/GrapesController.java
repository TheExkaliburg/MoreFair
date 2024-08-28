package de.kaliburg.morefair.api;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.game.vinegar.model.VinegarThrowEntity;
import de.kaliburg.morefair.game.vinegar.model.dto.VinegarThrowRecordResponse;
import de.kaliburg.morefair.game.vinegar.services.VinegarThrowService;
import de.kaliburg.morefair.game.vinegar.services.mapper.VinegarThrowMapper;
import de.kaliburg.morefair.security.SecurityUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping("/api/grapes")
@RequiredArgsConstructor
public class GrapesController {

  public static final String PRIVATE_VINEGAR_DESTINATION = "/vinegar/events";

  private final AccountService accountService;
  private final VinegarThrowService vinegarThrowService;
  private final VinegarThrowMapper vinegarThrowMapper;

  @GetMapping("/vinegar/throw/record")
  public ResponseEntity<VinegarThrowRecordResponse> getVinegarThrowRecords(
      Authentication authentication) {
    AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
        .orElse(null);

    if (account == null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    List<VinegarThrowEntity> listOfPastThrows = vinegarThrowService
        .findVinegarThrowsOfCurrentSeason(account.getId());

    VinegarThrowRecordResponse response = vinegarThrowMapper
        .mapVinegarThrowListToVinegarThrowRecords(listOfPastThrows, account.getId());

    return ResponseEntity.ok(response);
  }

}
