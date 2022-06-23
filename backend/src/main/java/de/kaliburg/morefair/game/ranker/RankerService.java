package de.kaliburg.morefair.game.ranker;

import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

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

  public RankerEntity createNewActiveRankerForAccountOnLadder(AccountEntity account, int i) {

  }
}
