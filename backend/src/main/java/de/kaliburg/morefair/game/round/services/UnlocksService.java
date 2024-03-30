package de.kaliburg.morefair.game.round.services;

import de.kaliburg.morefair.game.round.model.UnlocksEntity;
import java.util.Optional;

public interface UnlocksService {

  Optional<UnlocksEntity> findByAccountInCurrentRound(Long accountId);

  UnlocksEntity save(UnlocksEntity unlocks);

  UnlocksEntity createForAccountInCurrentRound(Long accountId);

  default UnlocksEntity findOrCreateByAccountInCurrentRound(Long accountId) {
    return findByAccountInCurrentRound(accountId)
        .orElse(createForAccountInCurrentRound(accountId));
  }
}
