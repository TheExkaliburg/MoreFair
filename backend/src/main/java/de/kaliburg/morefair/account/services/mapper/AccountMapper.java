package de.kaliburg.morefair.account.services.mapper;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.model.dto.AccountDetailsDto;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The Mapper that can convert the {@link AccountEntity AccountEntities} to DTOs.
 */
@Component
@RequiredArgsConstructor
public class AccountMapper {

  private final LadderService ladderService;

  private final RankerService rankerService;

  /**
   * Mapping the {@link AccountEntity} to a {@link AccountDetailsDto}.
   *
   * @param account the {@link AccountEntity}
   * @return the {@link AccountDetailsDto}
   */
  public AccountDetailsDto mapToAccountDetailsDto(AccountEntity account) {
    int ladderNumber = rankerService.findHighestCurrentRankerOfAccount(account)
        .map(r -> ladderService.findCurrentLadderById(r.getLadderId()))
        .map(r -> r.orElseThrow().getNumber())
        .orElse(1);

    return AccountDetailsDto.builder()
        .uuid(account.getUuid())
        .accountId(account.getId())
        .username(account.getUsername())
        .email(account.getUsername())
        .accessRole(account.getAccessRole())
        .highestCurrentLadder(ladderNumber)
        .build();
  }
}
