package de.kaliburg.morefair.game;

import de.kaliburg.morefair.game.round.RoundEntity;
import de.kaliburg.morefair.game.round.RoundService;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * The GameService that setups and manages the GameEntity. Also routes all the requests for the Game
 * itself.
 */
@Service
@Log4j2
public class GameService {

  private final GameRepository gameRepository;
  private final RoundService roundService;

  private GameEntity game;

  public GameService(GameRepository gameRepository, RoundService roundService) {
    this.gameRepository = gameRepository;
    this.roundService = roundService;
  }

  @PostConstruct
  private void initialGameSetup() {
    try {
      List<GameEntity> allGames = gameRepository.findAll();

      if (allGames.isEmpty()) {
        allGames.add(createNewGame());
      }

      game = allGames.get(0);

      saveToDatabase();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Scheduled(fixedDelay = 60000, fixedRate = 60000)
  private void saveToDatabase() {

  }

  private GameEntity createNewGame() {
    GameEntity result = new GameEntity();
    RoundEntity round = new RoundEntity();

    roundService.createNewRound(result);

    return result;
  }

}
