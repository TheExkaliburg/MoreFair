package de.kaliburg.morefair.chat.services.impl;

import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.chat.model.dto.SuggestionDto;
import de.kaliburg.morefair.chat.services.SuggestionsService;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuggestionsServiceImpl implements SuggestionsService {


  private final RankerService rankerService;
  private final AccountService accountService;

  @Override
  public List<SuggestionDto> getAllSuggestions() {
    List<RankerEntity> rankers = rankerService.findAllByCurrentLadderNumber(1);

    return rankers.stream()
        .map(ranker -> accountService.find(ranker.getAccountId()))
        .map(account -> SuggestionDto.builder()
            .accountId(account.getId())
            .displayName(account.getDisplayName())
            .build()
        )
        .toList();
  }
}
