package de.kaliburg.morefair.moderation.events.services.impl;

import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.events.types.LadderEventType;
import de.kaliburg.morefair.game.ladder.services.LadderService;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.moderation.events.model.UserEventEntity;
import de.kaliburg.morefair.moderation.events.model.UserEventMetadata;
import de.kaliburg.morefair.moderation.events.model.UserEventSnapshot;
import de.kaliburg.morefair.moderation.events.services.UserEventService;
import de.kaliburg.morefair.moderation.events.services.repositories.UserEventRepository;
import de.kaliburg.morefair.statistics.records.model.LadderRecord;
import de.kaliburg.morefair.statistics.records.model.RankerRecord;
import de.kaliburg.morefair.statistics.records.model.RoundRecord;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
@Slf4j
public class UserEventServiceImpl implements UserEventService {

  private final UserEventRepository userEventRepository;
  private final AccountService accountService;
  private final RankerService rankerService;
  private final LadderService ladderService;
  private final RoundService roundService;

  @Override
  @Transactional
  public UserEventEntity record(Long accountId, Enum<?> eventType, UserEventMetadata metadata) {
    OffsetDateTime createdOn = OffsetDateTime.now(ZoneOffset.UTC);
    log.info("UserEvent (#{}): {} - {}", accountId, eventType.toString(), metadata);
    UserEventSnapshot snapshot = captureSnapshot(accountId, eventType);
    UserEventEntity result = UserEventEntity.builder()
        .eventType(eventType.toString())
        .accountId(accountId)
        .createdOn(createdOn)
        .metadataVersion(metadata == null ? null : 1)
        .metadata(metadata)
        .snapshotVersion(snapshot == null ? null : 1)
        .snapshot(snapshot)
        .build();

    // TODO: Send to ModPage via WS
    result = userEventRepository.save(result);

    return result;
  }

  private UserEventSnapshot captureSnapshot(Long accountId, Enum<?> eventType) {
    var account = accountService.findById(accountId).orElseThrow();
    var ranker = rankerService.findHighestActiveRankerOfAccount(account);
    if (ranker.isEmpty()) {
      log.debug("UserEvent (#{}): {} has no active ranker; recording metadata only", accountId,
          eventType);
      return null;
    }

    var round = roundService.getCurrentRound();
    var ladder = ladderService.findLadderById(ranker.get().getLadderId()).orElseThrow();
    var rankers = rankerService.findAllByLadderId(ladder.getId());
    var firstLadder = ladder.getNumber() == 1 ? ladder
        : ladderService.findCurrentLadderWithNumber(1).orElseThrow();
    var firstRankers = firstLadder.getId().equals(ladder.getId()) ? rankers
        : rankerService.findAllByLadderId(firstLadder.getId());

    List<RankerRecord> targets = eventType == LadderEventType.THROW_VINEGAR
        ? rankers.stream().limit(2).map(RankerRecord::new).toList()
        : List.of();

    return new UserEventSnapshot(new RankerRecord(ranker.get()),
        new LadderRecord(ladder, rankers),
        new RoundRecord(round, new LadderRecord(firstLadder, firstRankers)),
        targets.isEmpty() ? null : targets.getFirst(),
        targets.size() < 2 ? null : targets.get(1));
  }
}
