package de.kaliburg.morefair.game.ranker.services;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import java.util.List;
import java.util.Optional;

public interface RankerService {

  /**
   * Finds the highest Ranker in the current Set of Ladders, that is being owned by the account.
   *
   * <p>This is a high performance function, which is basically called in every interaction the
   * player has with the Server.
   *
   * @param account The account, that the highest Ranker is searched of.
   * @return The highest Ranker of the account, if any exists.
   */
  Optional<RankerEntity> findHighestActiveRankerOfAccount(AccountEntity account);

  List<RankerEntity> findAllByCurrentLadderNumber(int ladderNumber);

  List<RankerEntity> findAllByLadderId(Long id);

  void enterNewRanker(AccountEntity account);

  Optional<RankerEntity> createRankerOnLadder(AccountEntity account, int ladderNumber);

  List<RankerEntity> updateRankersOfLadder(LadderEntity ladder, List<RankerEntity> rankers);

  void reloadRankers();
}
