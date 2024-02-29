package de.kaliburg.morefair.game.ranker.services;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.services.repositories.RankerRepository;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
public class RankerService {

  private final LoadingCache<Long, RankerEntity> rankerCache;

  private final RankerRepository rankerRepository;

  private final AccountService accountService;
  private final FairConfig fairConfig;

  public RankerService(RankerRepository rankerRepository, AccountService accountService,
      FairConfig fairConfig) {
    this.rankerCache = Caffeine.newBuilder()
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .build(id -> rankerRepository.findById(id).orElse(null));

    this.rankerRepository = rankerRepository;
    this.accountService = accountService;
    this.fairConfig = fairConfig;
  }

  @Transactional
  public RankerEntity createNewRanker(AccountEntity account, LadderEntity ladder, Integer rank) {
    RankerEntity result = RankerEntity.builder()
        .ladderId(ladder.getId())
        .accountId(account.getId())
        .rank(rank)
        .build();

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

  public List<RankerEntity> findCurrentRankersOfAccount(AccountEntity account, RoundEntity round) {
    return rankerRepository.findByAccountAndLadder_Round(account, round);
  }

  public List<RankerEntity> findCurrentActiveRankersOfAccount(AccountEntity account,
      RoundEntity round) {
    return rankerRepository.findByAccountAndLadder_RoundAndGrowingIsTrue(account, round);
  }

  @Transactional
  public List<RankerEntity> save(List<RankerEntity> rankers) {
    return rankerRepository.saveAll(rankers);
  }
}
