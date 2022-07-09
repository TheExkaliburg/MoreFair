package de.kaliburg.morefair.game.round;

import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.dto.LadderResultsDto;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The RoundService that setups and manages the RoundEntities contained in the GameEntity.
 */
@Service
@Log4j2
public class RoundService {

  private final RoundRepository roundRepository;
  private final LadderService ladderService;
  private final AccountService accountService;
  private LadderResultsDto lastRoundResults;

  public RoundService(RoundRepository roundRepository, LadderService ladderService,
      AccountService accountService) {
    this.roundRepository = roundRepository;
    this.ladderService = ladderService;
    this.accountService = accountService;
  }

  /**
   * Creates a new RoundEntity for the parent GameEntity, filled with an initial LadderEntity and
   * saves it.
   *
   * @return the newly created and saved RoundEntity with 1 Ladder
   */
  @Transactional
  public RoundEntity create(Integer number) {
    RoundEntity result = roundRepository.save(new RoundEntity(number));
    LadderEntity ladder = ladderService.createLadder(result, 1);
    result.getLadders().add(ladder);
    return result;
  }

  @Transactional
  public List<RoundEntity> save(List<RoundEntity> rounds) {
    return roundRepository.saveAll(rounds);
  }

  @Transactional
  public RoundEntity save(RoundEntity round) {
    return roundRepository.save(round);
  }

  /**
   * Overwrites the existing cached rounds with the current one round.
   *
   * @param round the current round
   */
  public void loadIntoCache(RoundEntity round) {
    ladderService.loadIntoCache(round);
  }

  public RoundEntity getCurrentRound() {
    return ladderService.getCurrentRound();
  }

  /**
   * Create a new Ranker and updates the highestAssholeCount if necessary.
   *
   * @param account the account that the ranker is part of
   * @return the ranker
   */
  public RankerEntity createNewRanker(AccountEntity account) {
    RankerEntity result = ladderService.createRanker(account);
    Integer timesAsshole = result.getAccount().getAssholeCount();

    if (timesAsshole > getCurrentRound().getHighestAssholeCount()) {
      // TODO: Global Event to update the highestLadder
      getCurrentRound().setHighestAssholeCount(timesAsshole);
      ladderService.setCurrentRound(save(getCurrentRound()));
    }

    return result;
  }


  public LadderResultsDto getLastRoundResults() {
    return lastRoundResults;
  }
}
