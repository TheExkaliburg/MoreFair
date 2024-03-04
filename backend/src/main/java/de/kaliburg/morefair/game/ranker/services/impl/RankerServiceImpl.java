package de.kaliburg.morefair.game.ranker.services.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.api.LadderController;
import de.kaliburg.morefair.api.RoundController;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.chat.model.dto.SuggestionDto;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.data.JoinData;
import de.kaliburg.morefair.events.types.LadderEventType;
import de.kaliburg.morefair.events.types.RoundEventTypes;
import de.kaliburg.morefair.game.ladder.model.LadderEntity;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.ranker.services.repositories.RankerRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
public class RankerServiceImpl implements RankerService {

  private final LoadingCache<Long, RankerEntity> rankerCache;
  private final RankerRepository rankerRepository;
  private final LadderService ladderService;
  private final WsUtils wsUtils;
  private final FairConfig fairConfig;

  public RankerServiceImpl(RankerRepository rankerRepository, LadderService ladderService,
      WsUtils wsUtils, FairConfig fairConfig) {
    this.rankerCache = Caffeine.newBuilder()
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .build(id -> rankerRepository.findById(id).orElse(null));

    this.rankerRepository = rankerRepository;
    this.ladderService = ladderService;
    this.wsUtils = wsUtils;
    this.fairConfig = fairConfig;
  }


  @Transactional
  public RankerEntity create(AccountEntity account, LadderEntity ladder, Integer rank) {
    RankerEntity result = RankerEntity.builder()
        .ladderId(ladder.getId())
        .accountId(account.getId())
        .rank(rank)
        .build();

    return rankerRepository.save(result);
  }

  @Override
  public void enterNewRanker(AccountEntity account) {
    createRankerOnLadder(account, 1);
  }

  @Override
  public List<RankerEntity> findAllByLadderId(Long id) {
    return List.of();
  }

  @Override
  public List<RankerEntity> findAllByCurrentLadderNumber(int ladderNumber) {
    // FIXME Logic with Caching
    return List.of();
  }

  @Override
  public Optional<RankerEntity> findHighestCurrentRankerOfAccount(AccountEntity account) {
    return Optional.empty();
  }

  private void createRankerOnLadder(AccountEntity account, int ladderNumber) {
    LadderEntity ladder = ladderService.findCurrentLadderWithNumber(ladderNumber)
        .orElse(ladderService.createCurrentLadder(ladderNumber));

    // TODO: Directly look up in the cache instead of using a function finding the highest
    boolean isAlreadyExisting = this.findHighestCurrentRankerOfAccount(account)
        .map(r -> ladderService.findCurrentLadderById(r.getLadderId()).orElseThrow())
        .filter(l -> l.getNumber() >= ladderNumber)
        .isPresent();

    // Only 1 active ranker per ladder
    if (isAlreadyExisting) {
      // FIXME return the existing ranker
      return;
    }

    // FIXME: use map/cache here
    RankerEntity result = create(account, ladder, ladder.getRankers().size() + 1);
    ladder.getRankers().add(result);

    if (ladder.getNumber() == 1) {
      Event<RoundEventTypes> joinEvent = new Event<>(RoundEventTypes.JOIN, account.getId());
      joinEvent.setData(new SuggestionDto(account.getId(), account.getDisplayName()));
      wsUtils.convertAndSendToTopic(RoundController.TOPIC_EVENTS_DESTINATION, joinEvent);
    }

    Event<LadderEventType> joinEvent = new Event<>(LadderEventType.JOIN, account.getId());
    joinEvent.setData(
        new JoinData(account.getDisplayName(), fairConfig.getAssholeTag(account.getAssholeCount()),
            account.getAssholePoints()));
    wsUtils.convertAndSendToTopicWithNumber(LadderController.TOPIC_EVENTS_DESTINATION, ladderNumber,
        joinEvent);
  }

}
