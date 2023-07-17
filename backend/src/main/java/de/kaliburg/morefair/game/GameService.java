package de.kaliburg.morefair.game;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountAccessRole;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.RoundController;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.RoundEventTypes;
import de.kaliburg.morefair.game.chat.ChatEntity;
import de.kaliburg.morefair.game.chat.ChatService;
import de.kaliburg.morefair.game.round.LadderService;
import de.kaliburg.morefair.game.round.RankerService;
import de.kaliburg.morefair.game.round.RoundEntity;
import de.kaliburg.morefair.game.round.RoundService;
import de.kaliburg.morefair.security.SecurityUtils;
import de.kaliburg.morefair.statistics.StatisticsService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The GameService that setups and manages the GameEntity. Also routes all the requests for the Game
 * itself.
 *
 * <ul>
 *   <li>{@link LadderService} for game logic, user events and everything that is contained in a
 *   ladder </li>
 *   <li>{@link RoundService} for statistics, global events and other round-specific things</li>
 *   <li>{@link ChatService} for chats and messages</li>
 * </ul>
 */
@Service
@Log4j2
public class GameService implements ApplicationListener<GameResetEvent> {

  private final GameRepository gameRepository;
  private final RoundService roundService;
  private final LadderService ladderService;
  private final RankerService rankerService;
  private final ChatService chatService;
  private final AccountService accountService;
  private final FairConfig config;
  private final StatisticsService statisticsService;
  private final SecurityUtils securityUtils;
  @Getter
  private GameEntity game;

  GameService(GameRepository gameRepository, RoundService roundService,
      LadderService ladderService, RankerService rankerService, ChatService chatService,
      AccountService accountService, FairConfig config, StatisticsService statisticsService,
      SecurityUtils securityUtils) {
    this.gameRepository = gameRepository;
    this.roundService = roundService;
    this.ladderService = ladderService;
    this.rankerService = rankerService;
    this.chatService = chatService;
    this.accountService = accountService;
    this.config = config;
    this.securityUtils = securityUtils;
    this.statisticsService = statisticsService;
  }

  @PostConstruct
  void initialGameSetup() {
    try {
      List<GameEntity> allGames = gameRepository.findAll();
      game = allGames.isEmpty() ? create() : allGames.get(0);
      roundService.loadIntoCache(game.getCurrentRound());
      chatService.loadIntoCache();
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @Transactional
  @Scheduled(initialDelay = 60 * 1000, fixedRate = 60 * 1000)
  @PreDestroy
  void saveStateToDatabase() {
    game = gameRepository.save(game);
    roundService.saveStateToDatabase();
    chatService.saveStateToDatabase();
  }

  private GameEntity create() {
    GameEntity result = new GameEntity();
    result = gameRepository.save(result);

    RoundEntity round = roundService.create(1);
    ChatEntity chat = chatService.create(1);
    result.setCurrentRound(round);

    if (accountService.findBroadcaster() == null) {
      String broadcasterUuid = UUID.randomUUID().toString();
      AccountEntity broadcaster = accountService.create(broadcasterUuid, broadcasterUuid, null,
          true);
      broadcaster.setDisplayName("Chad");
      broadcaster.setAccessRole(AccountAccessRole.BROADCASTER);
      broadcaster.setAssholePoints(config.getMaxAssholePointsAsTag());
      accountService.save(broadcaster);
    }

    return gameRepository.save(result);
  }


  @Transactional
  @Override
  public void onApplicationEvent(@NonNull GameResetEvent event) {
    long oldRoundId = game.getCurrentRound().getId();
    roundService.getCurrentRound().setClosedOn(OffsetDateTime.now(ZoneOffset.UTC));
    roundService.save(roundService.getCurrentRound());

    RoundEntity newRound = roundService.create(game.getCurrentRound().getNumber() + 1);
    game.setCurrentRound(newRound);
    game = gameRepository.save(game);
    chatService.saveStateToDatabase();
    initialGameSetup();

    statisticsService.startRoundStatistics(oldRoundId);
  }
}
