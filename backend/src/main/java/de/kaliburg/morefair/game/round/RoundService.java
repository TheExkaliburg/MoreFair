package de.kaliburg.morefair.game.round;

import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.game.ladder.LadderEntity;
import de.kaliburg.morefair.game.ladder.LadderService;
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
  public RoundEntity create(Long number) {
    RoundEntity result = roundRepository.save(new RoundEntity(number));
    LadderEntity ladder = ladderService.createLadder(result, 1);
    result.getLadders().add(ladder);
    return result;
  }

  /**
   * Updates existing RoundEntities and saves them.
   *
   * @param rounds the RoundEntities that need to be updated
   * @return the updated and saved RoundEntities
   */
  @Transactional
  public List<RoundEntity> updateRounds(List<RoundEntity> rounds) {
    return roundRepository.saveAll(rounds);
  }

  /**
   * Overwrites the existing cached rounds with the ones from this game.
   *
   * @param game the game that will have its current round cached
   */
  public void loadIntoCache(RoundEntity round) {
    ladderService.loadIntoCache(round);
  }

  /**
   * Adds and handles a global Event
   *
   * @param event the event to process
   */
  public void handleGlobalEvent(Event event) {
    // TODO: Switch case for all the events

    switch (event.getEventType()) {
      case NAME_CHANGE -> {
        accountService.updateUsername(accountService.find(event.getAccountId()),
            (String) event.getData());
      }
    }
  }
}
