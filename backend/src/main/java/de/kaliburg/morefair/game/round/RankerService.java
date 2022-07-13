package de.kaliburg.morefair.game.round;

import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import java.util.List;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
public class RankerService {

  private final RankerRepository rankerRepository;
  private final AccountService accountService;


  public RankerService(RankerRepository rankerRepository, @Lazy AccountService accountService) {
    this.rankerRepository = rankerRepository;
    this.accountService = accountService;
  }

  public RankerEntity findHighestActiveRankerOfAccountAndRound(AccountEntity account,
      RoundEntity currentRound) {
    return rankerRepository.findFirstByAccountAndLadder_RoundAndGrowingIsTrueOrderByLadder_NumberDesc(
        account, currentRound).orElse(null);
  }

  public RankerEntity create(AccountEntity account, LadderEntity ladder, Integer rank) {
    RankerEntity result = new RankerEntity(ladder, account, rank);
    if (ladder.getRound().getTypes().contains(RoundType.AUTO)
        && !ladder.getTypes().contains(LadderType.NO_AUTO)) {
      result.setAutoPromote(true);
    }

    return save(result);
  }

  @Transactional
  public RankerEntity save(RankerEntity ranker) {
    return rankerRepository.save(ranker);
  }

  public RankerEntity find(UUID uuid) {
    return rankerRepository.findByUuid(uuid).orElseThrow();
  }

  public RankerEntity find(Long id) {
    return rankerRepository.findById(id).orElseThrow();
  }

  @Transactional
  public List<RankerEntity> save(List<RankerEntity> rankers) {
    return rankerRepository.saveAll(rankers);
  }
}
