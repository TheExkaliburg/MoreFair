package de.kaliburg.morefair.game.ranker.services.mapper;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.model.dto.RankerDto;
import de.kaliburg.morefair.game.ranker.model.dto.RankerPrivateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The Mapper that can convert the {@link RankerEntity RankerEntities} to DTOs.
 */
@Component
@RequiredArgsConstructor
public class RankerMapper {

  private final AccountService accountService;
  private final FairConfig fairConfig;

  /**
   * Mapping a {@link RankerEntity} to a {@link RankerDto}.
   *
   * @param ranker The {@link RankerEntity}.
   * @return The {@link RankerDto}.
   */
  public RankerDto mapToRankerDto(RankerEntity ranker) {
    AccountEntity account = accountService.find(ranker.getAccountId());

    return RankerDto.builder()
        .accountId(ranker.getAccountId())
        .username(account.getDisplayName())
        .rank(ranker.getRank())
        .points(ranker.getPoints().toString())
        .power(ranker.getPower().toString())
        .bias(ranker.getBias())
        .multi(ranker.getMultiplier())
        .isGrowing(ranker.isGrowing())
        .assholeTag(fairConfig.getAssholeTag(account.getAssholeCount()))
        .assholePoints(account.getAssholePoints())
        .build();
  }


  /**
   * Mapping a {@link RankerEntity} to a {@link RankerPrivateDto}, containing Information like
   * Grapes/Vinegar/AutoPromote.
   *
   * @param ranker The {@link RankerEntity}.
   * @return The {@link RankerPrivateDto}.
   */
  public RankerPrivateDto mapToPrivateDto(RankerEntity ranker) {
    AccountEntity account = accountService.find(ranker.getAccountId());

    return RankerPrivateDto.builder()
        .grapes(ranker.getGrapes().toString())
        .vinegar(ranker.getVinegar().toString())
        .autoPromote(ranker.isAutoPromote())
        .accountId(ranker.getAccountId())
        .username(account.getDisplayName())
        .rank(ranker.getRank())
        .points(ranker.getPoints().toString())
        .power(ranker.getPower().toString())
        .bias(ranker.getBias())
        .multi(ranker.getMultiplier())
        .isGrowing(ranker.isGrowing())
        .assholeTag(fairConfig.getAssholeTag(account.getAssholeCount()))
        .assholePoints(account.getAssholePoints())
        .build();
  }

}
