package de.kaliburg.morefair.game.ladder.services.mapper;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.model.dto.LadderDto;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.model.dto.RankerDto;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.ranker.services.mapper.RankerMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


/**
 * The Mapper that can convert the {@link LadderEntity LadderEntities} to DTOs.
 */
@Component
@RequiredArgsConstructor
public class LadderMapper {

  private final RankerService rankerService;
  private final RankerMapper rankerMapper;

  /**
   * Mapping the {@link LadderEntity} to a {@link LadderDto}.
   * <p/>
   * This also needs a {@link AccountEntity} since this {@link LadderDto} is out of the perspective
   * of a user.<br>So we need to differentiate between private and public
   * {@link RankerDto RankerDtos}.
   *
   * @param ladder  The {@link LadderEntity}
   * @param account The {@link AccountEntity} from which perspective this is mapped.
   * @return the {@link LadderDto} from the perspective of {@link AccountEntity}
   */
  public LadderDto mapToLadderDto(LadderEntity ladder, AccountEntity account) {
    List<RankerEntity> rankers = rankerService.findAllByLadderId(ladder.getId());

    return LadderDto.builder()
        .number(ladder.getNumber())
        .scaling(ladder.getScaling())
        .types(ladder.getTypes())
        .basePointsToPromote(ladder.getBasePointsToPromote().toString())
        .rankers(
            rankers.stream().map(
                r -> r.getAccountId().equals(account.getId()) ? rankerMapper.mapToPrivateDto(r)
                    : rankerMapper.mapToRankerDto(r)
            ).toList()
        )
        .build();
  }

}
