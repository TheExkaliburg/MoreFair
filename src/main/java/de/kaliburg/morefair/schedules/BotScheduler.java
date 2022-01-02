package de.kaliburg.morefair.schedules;

import de.kaliburg.morefair.multithreading.DatabaseWriteSemaphore;
import de.kaliburg.morefair.service.AccountService;
import de.kaliburg.morefair.service.LadderService;
import de.kaliburg.morefair.service.RankerService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.ThreadLocalRandom;

@EnableScheduling
public class BotScheduler {
    private final LadderService ladderService;
    private final RankerService rankerService;
    private final AccountService accountService;

    public BotScheduler(LadderService ladderService, RankerService rankerService, AccountService accountService) {
        this.ladderService = ladderService;
        this.rankerService = rankerService;
        this.accountService = accountService;
    }

    @Scheduled(initialDelay = 30000, fixedDelay = 5000)
    public void update() {
        DatabaseWriteSemaphore.getInstance().aquireAndAutoReleaseSilent(() -> {
            if (rankerService.findAllRankerByLadder(ladderService.findAllLadders().get(0)).size() < 10
                    && ThreadLocalRandom.current().nextInt(0, 10) == 0)
                accountService.createNewAccount();
        });
    }
}
