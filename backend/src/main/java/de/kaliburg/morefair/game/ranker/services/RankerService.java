package de.kaliburg.morefair.game.ranker.services;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import java.util.List;
import java.util.Optional;

public interface RankerService {

  Optional<RankerEntity> findHighestCurrentRankerOfAccount(AccountEntity account);

  List<RankerEntity> findAllByCurrentLadderNumber(int number);

  List<RankerEntity> findAllByLadderId(Long id);

  void enterNewRanker(AccountEntity account);
}
