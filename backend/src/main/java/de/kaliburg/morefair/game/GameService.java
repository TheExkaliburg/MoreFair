package de.kaliburg.morefair.game;

import de.kaliburg.morefair.account.entity.AccountEntity;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.game.chat.ChatEntity;
import de.kaliburg.morefair.game.chat.ChatService;
import de.kaliburg.morefair.game.round.RoundEntity;
import de.kaliburg.morefair.game.round.RoundService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import javax.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The GameService that setups and manages the GameEntity. Also routes all the requests for the Game itself.
 */
@Service
@Log4j2
public class GameService {

    private final GameRepository gameRepository;
    private final RoundService roundService;
    private final ChatService chatService;
    private final Semaphore globalEventSemaphore = new Semaphore(1);
    private GameEntity game;
    private List<Event> globalEventList = new ArrayList<>();

    public GameService(GameRepository gameRepository, RoundService roundService,
        ChatService chatService) {
        this.gameRepository = gameRepository;
        this.roundService = roundService;
        this.chatService = chatService;
    }

    @PostConstruct
    private void initialGameSetup() {
        try {
            List<GameEntity> allGames = gameRepository.findAll();
            game = allGames.isEmpty() ? createGame() : allGames.get(0);
            roundService.loadIntoCache(game);
            chatService.loadIntoCache(game);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedDelay = 60000, fixedRate = 60000)
    @Transactional
    public void saveToDatabase() {
        try {
            updateGame(this.game);
        } catch (Exception e) {

        }
    }

    private GameEntity createGame() {
        GameEntity result = gameRepository.save(new GameEntity());
        RoundEntity round = roundService.createRound(result, 1);
        result.setCurrentRound(round);
        result.getRounds().add(round);

        ChatEntity chat = chatService.createChat(result, 1);
        result.getChats().add(chat);

        return gameRepository.save(result);
    }

    private GameEntity updateGame(GameEntity game) {
        roundService.updateRounds(List.of(game.getCurrentRound()));
        chatService.updateChats(game.getChats());

        return game;
    }

    public void addEvent(AccountEntity account, Event event) {
        roundService.addEvent(account, event);
    }

    public void addGlobalEvent(Event event) {
        try {
            globalEventSemaphore.acquire();
            try {
                globalEventList.add(event);
            } finally {
                globalEventSemaphore.release();
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

}
