package de.kaliburg.morefair.ladder;

import de.kaliburg.morefair.FairController;
import de.kaliburg.morefair.account.entity.Account;
import de.kaliburg.morefair.account.events.AccountServiceEvent;
import de.kaliburg.morefair.account.service.AccountService;
import de.kaliburg.morefair.chat.MessageService;
import de.kaliburg.morefair.dto.LadderViewDTO;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.EventType;
import de.kaliburg.morefair.events.data.JoinData;
import de.kaliburg.morefair.events.data.VinegarData;
import de.kaliburg.morefair.moderation.data.ModUpdateData;
import de.kaliburg.morefair.utils.UpgradeUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Semaphore;

@Service
@Log4j2
public class RankerService implements ApplicationListener<AccountServiceEvent> {
    private final RankerRepository rankerRepository;
    private final LadderRepository ladderRepository;
    private final MessageService messageService;
    @Getter
    private final Map<Integer, List<Event>> eventMap = new HashMap<>();
    @Getter
    private final List<Event> globalEventList = new ArrayList<>();
    @Getter
    private final Semaphore ladderSem = new Semaphore(1);
    @Getter
    private final Semaphore eventSem = new Semaphore(1);
    @Getter
    private final Map<Integer, Ladder> ladders = new HashMap<>();
    private final AccountService accountService;

    public RankerService(RankerRepository rankerRepository, LadderRepository ladderRepository, MessageService messageService, @Lazy AccountService accountService) {
        this.rankerRepository = rankerRepository;
        this.ladderRepository = ladderRepository;
        this.messageService = messageService;
        this.accountService = accountService;
    }

    /*@PostConstruct
    @Transactional
    public void test() {
        Ladder ladder = ladderRepository.findAllLaddersJoinedWithRankers().stream().toList().get(0);
        Account account = new Account(UUID.randomUUID(), "test");
        Ranker ranker = new Ranker(UUID.randomUUID(), ladder, account, ladder.getRankers().size() + 1);

        account = accountService.saveAccount(account);
        ranker = rankerRepository.save(ranker);

        account.setUsername("test2");
        ranker.setPoints(ranker.getPoints().add(BigInteger.ONE));
        rankerRepository.saveAll(List.of(ranker));
        System.out.println(ranker.getAccount().getUsername());
    }*/

    @PostConstruct
    public void init() {
        try {
            ladderSem.acquire();
            try {
                Ladder ladder = ladderRepository.findByNumber(1);
                if (ladder == null) {
                    ladder = createNewLadder(1);
                    ladders.put(ladder.getNumber(), ladder);
                    messageService.addChat(ladder);
                }

                ladderRepository.findAllLaddersJoinedWithRankers().forEach(l -> ladders.put(l.getNumber(), l));
                for (Ladder l : ladders.values()) {
                    eventMap.put(l.getNumber(), new ArrayList<>());
                }
            } finally {
                ladderSem.release();
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional
    @Scheduled(initialDelay = 60000, fixedRate = 60000)
    @PreDestroy
    public void syncWithDB() {
        log.debug("Saving Ladders...");
        try {
            ladderSem.acquire();
            try {
                for (Ladder ladder : ladders.values()) {
                    rankerRepository.saveAll(ladder.getRankers());
                }
                ladderRepository.saveAll(ladders.values());
            } finally {
                ladderSem.release();
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        log.trace("Ladders are saved!");
    }

    // SEARCHES

    public LadderViewDTO findAllRankerByLadderAreaAndAccount(Integer ladderNum, Account account) {
        Ladder currentLadder = findLadder(ladderNum);
        Ranker currentRanker = findActiveRankerOfAccountOnLadder(account.getId(), currentLadder);

        assert (currentRanker.getAccount().getUuid().equals(account.getUuid()));
        assert (currentRanker.getAccount().getUsername().equals(account.getUsername()));

        List<Ranker> result = findAllRankerByLadderOrderedByPoints(currentLadder);

        LadderViewDTO ladderView = new LadderViewDTO(result, currentLadder, account, findHighestRankerByLadder(currentLadder));
        return ladderView;
    }

    public Ranker findHighestActiveRankerByAccount(Account account) {
        if (account.getRankers().size() == 0)
            return null;
        try {
            return Collections.max(account.getRankers().stream()
                    .filter(Ranker::isGrowing).toList(), Comparator.comparing(r -> r.getLadder().getNumber()));
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public Ranker findHighestRankerByLadder(Ladder ladder) {
        return Collections.max(findLadder(ladder).getRankers(), Comparator.comparing(Ranker::getPoints));
    }

    public List<Ranker> findAllRankerByLadderOrderedByPoints(Ladder ladder) {
        ladder.getRankers().sort(Comparator.comparing(Ranker::getPoints).reversed());
        return ladder.getRankers();
    }

    public Ladder findLadder(Integer ladderNum) {
        return ladders.get(ladderNum);
    }

    public Ladder findLadder(Ladder ladder) {
        return this.ladders.get(ladder.getNumber());
    }


    public void addEvent(Integer ladderNum, Event event) {
        try {
            eventSem.acquire();
            try {
                eventMap.get(ladderNum).add(event);
            } finally {
                eventSem.release();
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public void addGlobalEvent(Event event) {
        try {
            eventSem.acquire();
            try {
                globalEventList.add(event);
            } finally {
                eventSem.release();
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public void resetEvents() {
        eventMap.values().forEach(List::clear);
        globalEventList.clear();
    }

    public Ranker createNewActiveRankerForAccountOnLadder(Account account, Integer ladderNum) {
        Ladder ladder = findLadder(ladderNum);

        if (ladder == null) {
            ladder = createNewLadder(ladderNum);
            ladder = findLadder(ladder);
        }

        account = accountService.findAccountByUUID(account.getUuid());
        List<Ranker> activeRankersInLadder = account.getRankers().stream()
                .filter(r -> r.getLadder().getNumber().equals(ladderNum) && r.isGrowing()).toList();
        Ranker ranker;

        if (activeRankersInLadder.isEmpty()) {
            ranker = saveRanker(new Ranker(UUID.randomUUID(), ladder, account, ladder.getRankers().size() + 1));
            ladder.getRankers().add(ranker);
            Event event = new Event(EventType.JOIN, account.getId());
            event.setData(new JoinData(StringEscapeUtils.unescapeJava(account.getUsername()), account.getTimesAsshole()));
            eventMap.get(ladderNum).add(event);
        } else {
            ranker = activeRankersInLadder.get(0);
        }

        return ranker;
    }

    @Transactional
    protected Ranker saveRanker(Ranker ranker) {
        return rankerRepository.save(ranker);
    }


    protected Ladder createNewLadder(Integer ladderNum) {
        Ladder ladder = saveLadder(new Ladder(UUID.randomUUID(), ladderNum));

        ladders.put(ladderNum, ladder);
        eventMap.put(ladderNum, new ArrayList<>());
        messageService.addChat(ladder);
        return ladder;
    }

    @Transactional
    protected Ladder saveLadder(Ladder ladder) {
        return ladderRepository.save(ladder);
    }

    // Event Actions

    public boolean buyBias(Long accountId, Ladder ladder) {
        try {
            Ranker ranker = findActiveRankerOfAccountOnLadder(accountId, ladder);
            BigInteger cost = UpgradeUtils.buyUpgradeCost(ranker.getLadder().getNumber(), ranker.getBias());
            if (ranker.getPoints().compareTo(cost) >= 0) {
                ranker.setPoints(BigInteger.ZERO);
                ranker.setBias(ranker.getBias() + 1);
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean buyMulti(Long accountId, Ladder ladder) {
        try {
            Ranker ranker = findActiveRankerOfAccountOnLadder(accountId, ladder);
            BigInteger cost = UpgradeUtils.buyUpgradeCost(ranker.getLadder().getNumber(), ranker.getMultiplier());
            if (ranker.getPower().compareTo(cost) >= 0) {
                ranker.setPoints(BigInteger.ZERO);
                ranker.setPower(BigInteger.ZERO);
                ranker.setBias(0);
                ranker.setMultiplier(ranker.getMultiplier() + 1);
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean promote(Long accountId, Ladder ladder, Boolean isAssholeEvent) {
        try {
            Ranker ranker = findActiveRankerOfAccountOnLadder(accountId, ladder);
            if (ranker == null) return false;

            BigInteger neededPointDiff = BigInteger.ZERO;

            if (ladder.getRankers().size() <= ranker.getRank()) return false;
            Ranker pursuingRanker = ladder.getRankers().get(ranker.getRank());
            if (pursuingRanker == null) return false;

            // How many points the ranker is in front of his pursuer
            BigInteger pointDiff = ranker.getPoints().subtract(pursuingRanker.getPoints());

            if (ladder.getNumber() >= FairController.AUTO_PROMOTE_LADDER) {
                // How many more points does the ranker gain against his pursuer, every Second
                BigInteger powerDiff = ranker.getPower().subtract(
                        pursuingRanker.isGrowing() ? pursuingRanker.getPower() : BigInteger.ZERO);
                // Calculate the needed Point difference, to have f.e. 15seconds of point generation with the difference in power
                neededPointDiff = powerDiff.multiply(BigInteger.valueOf(FairController.MANUAL_PROMOTE_WAIT_TIME)).abs();
            }

            // If
            // - Ranker is #1
            // - There are enough people to promote
            // - Ranker got enough points to promote
            // - Ranker has either:
            //      - Auto-Promote
            //      - enough points to be in front of the next ranker
            if (ranker.getRank() == 1 && ranker.getLadder().getRankers().size() >= Math.max(FairController.MINIMUM_PEOPLE_FOR_PROMOTE, ladder.getNumber())
                    && ranker.getPoints().compareTo(FairController.POINTS_FOR_PROMOTE.multiply(BigInteger.valueOf(ranker.getLadder().getNumber()))) >= 0
                    && (ranker.isAutoPromote() || pointDiff.compareTo(neededPointDiff) >= 0)) {

                log.info("[L{}] Promotion for {}", ladder.getNumber(), ranker.getAccount().getUsername());
                ranker.setGrowing(false);
                saveRanker(ranker);
                Ranker newRanker = createNewActiveRankerForAccountOnLadder(ranker.getAccount(), ranker.getLadder().getNumber() + 1);
                newRanker.setVinegar(ranker.getVinegar());
                newRanker.setGrapes(ranker.getGrapes());
                Ladder newLadder = findLadder(newRanker.getLadder());

                if (newLadder.getRankers().size() == 1) {
                    newRanker.setAutoPromote(true);
                }
                if ((isAssholeEvent || ranker.isAutoPromote()) && ranker.getLadder().getNumber().compareTo(FairController.BASE_ASSHOLE_LADDER + accountService.findMaxTimesAsshole()) == 0) {
                    Account account = accountService.findByUuid(ranker.getAccount().getUuid());
                    account.setIsAsshole(true);
                    accountService.saveAccount(account);
                }
                saveRanker(newRanker);
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return false;
    }


    @Deprecated
    public boolean beAsshole(Long accountId, Ladder ladder) {
        try {
            Ranker ranker = findActiveRankerOfAccountOnLadder(accountId, ladder);
            // If
            // - Ranker is #1
            // - There are enough people to promote
            // - Ranker got enough points to promote
            // - The current Ladder is the assholeLadder or higher
            if (ranker.getRank() == 1 && ranker.getLadder().getRankers().size() >= Math.max(FairController.MINIMUM_PEOPLE_FOR_PROMOTE, ladder.getNumber())
                    && ranker.getPoints().compareTo(FairController.POINTS_FOR_PROMOTE.multiply(BigInteger.valueOf(ladder.getNumber()))) >= 0
                    && ranker.getLadder().getNumber().compareTo(FairController.BASE_ASSHOLE_LADDER + accountService.findMaxTimesAsshole()) >= 0) {
                Account account = accountService.findByUuid(ranker.getAccount().getUuid());
                account.setIsAsshole(true);
                accountService.saveAccount(account);

                // Promote the Ranker afterwards
                eventMap.get(ladder.getNumber()).add(new Event(EventType.PROMOTE, ranker.getAccount().getId()));
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return false;
    }


    public boolean throwVinegar(Long accountId, Ladder ladder, Event event) {
        try {
            Ranker ranker = findActiveRankerOfAccountOnLadder(accountId, ladder);
            Ranker target = findHighestRankerByLadder(ranker.getLadder());

            // If
            // - Target is #1
            // - Target is not you
            // - Target is active on that Ladder
            // - There are enough people to promote
            // - Target got enough points to promote
            // - Ranker got enough Vinegar to throw
            if (target.getRank() == 1 && ranker.getUuid() != target.getUuid() && target.isGrowing()
                    && target.getLadder().getRankers().size() >= Math.max(FairController.MINIMUM_PEOPLE_FOR_PROMOTE, ladder.getNumber())
                    && target.getPoints().compareTo(FairController.POINTS_FOR_PROMOTE.multiply(BigInteger.valueOf(ladder.getNumber()))) >= 0
                    && ranker.getVinegar().compareTo(UpgradeUtils.throwVinegarCost(target.getLadder().getNumber())) >= 0) {
                if (target.isAutoPromote()) {
                    log.info("[L{}] User {} tried to throw Vinegar at {}, but they had Auto-Promote!", ladder.getNumber(), ranker.getAccount().getUsername(), target.getAccount().getUsername());
                    eventMap.get(ladder.getNumber()).add(new Event(EventType.PROMOTE, target.getAccount().getId()));
                    return false;
                }

                BigInteger rankerVinegar = ranker.getVinegar();
                BigInteger targetVinegar = target.getVinegar();
                log.info("[L{}] User {} is using their {} Vinegar on the User {} with {} Vinegar", ladder.getNumber(), ranker.getAccount().getUsername(), rankerVinegar, target.getAccount().getUsername(), targetVinegar);
                VinegarData data = new VinegarData(rankerVinegar.toString());
                if (targetVinegar.compareTo(rankerVinegar) > 0) {
                    targetVinegar = targetVinegar.subtract(rankerVinegar);
                } else {
                    targetVinegar = BigInteger.ZERO;

                    data.setSuccess(true);
                    eventMap.get(ladder.getNumber()).add(new Event(EventType.MULTI, target.getAccount().getId()));
                    eventMap.get(ladder.getNumber()).add(new Event(EventType.SOFT_RESET_POINTS, target.getAccount().getId()));
                }
                event.setData(data);
                ranker.setVinegar(BigInteger.ZERO);
                target.setVinegar(targetVinegar);
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean resetAllLadders() {
        try {
            List<Account> accounts = accountService.findAllAccountsJoinedWithRankers().stream().toList();
            long assholeCount = accounts.stream().filter(Account::getIsAsshole).count();

            int assholeLadder = FairController.BASE_ASSHOLE_LADDER + accountService.findMaxTimesAsshole();
            int neededAssholesForReset = Math.max(FairController.ASSHOLES_FOR_RESET, (assholeLadder + 1) >> 1);

            if (assholeCount >= neededAssholesForReset) {
                deleteAllRanker();
                for (Ladder ladder : ladders.values()) {
                    ladder = ladderRepository.findLadderByUUIDWithRanker(ladder.getUuid());
                    ladders.put(ladder.getNumber(), ladder);
                }
                for (Account account : accounts) {
                    account = accountService.findByUuid(account.getUuid());
                    account.setTimesAsshole(account.getTimesAsshole() + (account.getIsAsshole() ? 1 : 0));
                    account.setIsAsshole(false);
                    // If Account was active in the last 7 days
                    // This should prevent old and inactive accounts from getting a ranker on Restart
                    if (account.getLastLogin().plus(0, ChronoUnit.HOURS).isAfter(LocalDateTime.now())) {
                        // Create New Ranker
                        // createNewActiveRankerForAccountOnLadder(account, 1);
                    }
                    accountService.saveAccount(account);
                }
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return false;
    }

    @Transactional
    protected void deleteAllRanker() {
        rankerRepository.deleteAll();
    }

    private Ranker findActiveRankerOfAccountOnLadder(Long accountId, Ladder ladder) {
        return findLadder(ladder).getRankers().stream().filter(r -> r.getAccount().getId().equals(accountId) && r.isGrowing()).findFirst().orElse(null);
    }


    public boolean buyAutoPromote(Long accountId, Ladder ladder) {
        try {
            Ranker ranker = findActiveRankerOfAccountOnLadder(accountId, ladder);
            BigInteger cost = UpgradeUtils.buyAutoPromoteCost(ranker.getRank(), ranker.getLadder().getNumber());
            if (!ranker.isAutoPromote() && ranker.getGrapes().compareTo(cost) >= 0
                    && ladder.getNumber() >= FairController.AUTO_PROMOTE_LADDER
                    && ladder.getNumber() != FairController.BASE_ASSHOLE_LADDER + accountService.findMaxTimesAsshole()) {
                log.info("[L{}] Buying Auto-Promote for {}", ladder.getNumber(), ranker.getAccount().getUsername());
                ranker.setGrapes(ranker.getGrapes().subtract(cost));
                ranker.setAutoPromote(true);
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean softResetPoints(Long accountId, Ladder ladder) {
        try {
            Ranker ranker = findActiveRankerOfAccountOnLadder(accountId, ladder);
            ranker.setPoints(BigInteger.ZERO);
            ModUpdateData modUpdateData = new ModUpdateData();
            modUpdateData.setLadderNumber(ladder.getNumber());
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Ladder getHighestLadder() {
        return ladders.values().stream().max(Comparator.comparing(Ladder::getNumber)).orElse(null);
    }

    @Override
    public void onApplicationEvent(AccountServiceEvent event) {
        switch (event.getEventType()) {
            case CREATE -> onCreatedAccount(event);
            case UPDATE -> onUpdatedAccount(event);
        }
    }

    private void onCreatedAccount(AccountServiceEvent event) {
        Ranker ranker = createNewActiveRankerForAccountOnLadder(event.getAccount(), 1);
        event.getAccount().getRankers().add(ranker);
    }

    private void onUpdatedAccount(AccountServiceEvent event) {
        for (Ranker accRanker : event.getAccount().getRankers()) {
            Ladder ladder = findLadder(accRanker.getLadder());
            for (Ranker ranker : ladder.getRankers()) {
                if (ranker.getAccount().getId().equals(event.getAccount().getId())) {
                    ranker.setAccount(event.getAccount());
                }
            }
        }
    }
}
