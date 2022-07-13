package de.kaliburg.morefair.account;

import de.kaliburg.morefair.game.round.RoundEntity;
import javax.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class UnlockService {

  private final UnlockRepository unlockRepository;

  public UnlockService(UnlockRepository unlockRepository) {
    this.unlockRepository = unlockRepository;
  }

  public UnlockEntity create(AccountEntity account, RoundEntity currentRound) {
    UnlockEntity result = new UnlockEntity(account, currentRound);

    return save(result);
  }

  @Transactional
  public UnlockEntity save(UnlockEntity unlock) {
    return unlockRepository.save(unlock);
  }

  public UnlockEntity find(AccountEntity account, RoundEntity currentRound) {
    return unlockRepository.findByAccountAndRound(account, currentRound).orElse(null);
  }
}
