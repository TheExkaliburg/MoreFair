package de.kaliburg.morefair.game.round.services.impl;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.api.RoundController;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.RoundEventTypes;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.model.RoundType;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.game.round.services.RoundUtils;
import de.kaliburg.morefair.game.round.services.repositories.RoundRepository;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The RoundService that setups and manages the RoundEntities contained in the GameEntity.
 */
@Service
@Log4j2
public class RoundServiceImpl implements RoundService {

  private final RoundRepository roundRepository;
  private final LadderService ladderService;
  private final WsUtils wsUtils;
  private final RoundUtils roundUtils;
  private final FairConfig config;

  public RoundServiceImpl(RoundRepository roundRepository, LadderService ladderService,
      @Lazy WsUtils wsUtils, RoundUtils roundUtils, FairConfig config) {
    this.roundRepository = roundRepository;
    this.ladderService = ladderService;
    this.wsUtils = wsUtils;
    this.roundUtils = roundUtils;
    this.config = config;
  }

  /**
   * Creates a new RoundEntity for the parent GameEntity, filled with an initial LadderEntity and
   * saves it.
   *
   * @return the newly created and saved RoundEntity with 1 Ladder
   */
  @Transactional
  public RoundEntity create(Integer number) {
    RoundEntity previousRound = find(number - 1).orElse(null);
    RoundEntity result = new RoundEntity(number, config, previousRound);
    result = roundRepository.save(result);
    LadderEntity ladder = ladderService.createCurrentLadder(1);
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

  public RoundEntity getCurrentRound() {
    return ladderService.getCurrentRound();
  }

  public Optional<RoundEntity> find(Integer number) {
    if (getCurrentRound() != null && getCurrentRound().getNumber().equals(number)) {
      return Optional.of(getCurrentRound());
    }

    return roundRepository.findByNumber(number);
  }

  /**
   * Create a new Ranker and updates the highestAssholeCount if necessary.
   *
   * @param account the account that the ranker is part of
   * @return the ranker
   */
  public RankerEntity enterRound(AccountEntity account) {
    // FIXME turn into update RoundStats

    Integer assholeCount = result.getAccountId().getAssholeCount();
    int baseAssholeLadderNumber = getCurrentRound().getTypes().contains(RoundType.FAST)
        ? getCurrentRound().getBaseAssholeLadder() / 2
        : getCurrentRound().getBaseAssholeLadder();

    if (assholeCount > getCurrentRound().getHighestAssholeCount()
        && !getCurrentRound().getTypes().contains(RoundType.CHAOS)
        && ladderService.findInCache(baseAssholeLadderNumber) == null) {
      getCurrentRound().setHighestAssholeCount(assholeCount);
      ladderService.setCurrentRound(save(getCurrentRound()));
      wsUtils.convertAndSendToTopic(RoundController.TOPIC_EVENTS_DESTINATION, new Event<>(
          RoundEventTypes.INCREASE_ASSHOLE_LADDER, account.getId(),
          roundUtils.getAssholeLadderNumber(getCurrentRound())));
    }

    return result;
  }

}
