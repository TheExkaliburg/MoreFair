package de.kaliburg.morefair.game.ranker;

import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.game.ladder.LadderEntity;
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

  public RankerEntity findHighestActiveRankerOfAccount(AccountEntity account) {
    return rankerRepository.findFirstByAccountAndGrowingIsTrueOrderByLadder_Round_NumberDescLadder_NumberDesc(
        account).orElseThrow();
  }

  public RankerEntity create(AccountEntity account, LadderEntity ladder, Integer rank) {
    RankerEntity result = new RankerEntity(ladder, account, rank);

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
}
