package de.kaliburg.morefair.game.round.services.impl;

import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.UnlocksEntity;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.game.round.services.UnlocksService;
import de.kaliburg.morefair.game.round.services.repositories.UnlocksRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnlocksServiceImpl implements UnlocksService {

  private final UnlocksRepository repository;
  private final RoundService roundService;

  @Override
  public Optional<UnlocksEntity> findByAccountInCurrentRound(Long accountId) {
    RoundEntity currentRound = roundService.getCurrentRound();
    return repository.findFirstByAccountIdAndRoundId(accountId, currentRound.getId());
  }

  @Override
  @Transactional
  public UnlocksEntity save(UnlocksEntity u) {
    return repository.save(u);
  }

  @Override
  @Transactional
  public UnlocksEntity createForAccountInCurrentRound(Long accountId) {
    RoundEntity currentRound = roundService.getCurrentRound();
    UnlocksEntity result = UnlocksEntity.builder()
        .accountId(accountId)
        .roundId(currentRound.getId())
        .build();

    return repository.save(result);
  }
}
