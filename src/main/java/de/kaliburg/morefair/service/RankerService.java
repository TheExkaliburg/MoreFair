package de.kaliburg.morefair.service;

import de.kaliburg.morefair.controller.FairController;
import de.kaliburg.morefair.dto.EventDTO;
import de.kaliburg.morefair.dto.LadderViewDTO;
import de.kaliburg.morefair.persistence.entity.Account;
import de.kaliburg.morefair.persistence.entity.Ladder;
import de.kaliburg.morefair.persistence.entity.Ranker;
import de.kaliburg.morefair.persistence.repository.AccountRepository;
import de.kaliburg.morefair.persistence.repository.LadderRepository;
import de.kaliburg.morefair.persistence.repository.RankerRepository;
import de.kaliburg.morefair.utils.UpgradeUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Semaphore;

@Service
@Log4j2
public class RankerService {
    private final RankerRepository rankerRepository;
    private final LadderRepository ladderRepository;
    private final AccountRepository accountRepository;
    private final MessageService messageService;
    @Getter
    private final Map<Integer, List<EventDTO>> eventMap = new HashMap<>();
    @Getter
    private final Semaphore ladderSem = new Semaphore(1);
    @Getter
    private final Semaphore eventSem = new Semaphore(1);
    @Getter
    private List<Ladder> ladders = new ArrayList<>();

    public RankerService(RankerRepository rankerRepository, LadderRepository ladderRepository, AccountRepository accountRepository, MessageService messageService) {
        this.rankerRepository = rankerRepository;
        this.ladderRepository = ladderRepository;
        this.accountRepository = accountRepository;
        this.messageService = messageService;
    }

    @PostConstruct
    public void init() {
        try {
            ladderSem.acquire();
            try {
                if (ladderRepository.findByNumber(1) == null) {
                    messageService.getChats().add(ladderRepository.save(createNewLadder(1)));
                }

                ladders = ladderRepository.findAllLaddersJoinedWithRankers().stream().toList();
                for (Ladder l : ladders) {
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

    @Scheduled(initialDelay = 60000, fixedRate = 60000)
    public void syncWithDB() {
        // TODO: Sync with DB
        try {
            ladderSem.acquire();
            try {
                ladderRepository.saveAll(ladders);
                for (Ladder ladder : ladders) {
                    rankerRepository.saveAll(ladder.getRankers());
                }
            } finally {
                ladderSem.release();
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    // SEARCHES

    public LadderViewDTO findAllRankerByHighestLadderAreaAndAccount(Account account) {
        Ranker currentRanker = findHighestRankerByAccount(account);
        Ladder currentLadder = findLadder(currentRanker.getLadder());

        assert (currentRanker.getAccount().getUuid().equals(account.getUuid()));
        assert (currentRanker.getAccount().getUsername().equals(account.getUsername()));

        List<Ranker> result = findAllRankerByLadder(currentLadder);

        LadderViewDTO ladderView = new LadderViewDTO(result, currentLadder, account, findHighestRankerByLadder(currentLadder));

        return ladderView;
    }

    public Ranker findHighestRankerByAccount(Account account) {
        Ranker ranker = Collections.max(account.getRankers(), Comparator.comparing(r -> r.getLadder().getNumber()));

        if (ranker == null) {
            ranker = createNewRankerForAccountOnLadder(account, 1);
        }

        ranker.setLadder(findLadder(ranker.getLadder()));
        return ranker;
    }

    public Ranker findHighestRankerByLadder(Ladder ladder) {
        Ranker ranker = Collections.max(findLadder(ladder).getRankers(), Comparator.comparing(Ranker::getPoints));
        return ranker;
    }

    public List<Ranker> findAllRankerByLadder(Ladder ladder) {
        return ladder.getRankers();
    }

    public List<Ranker> findAllRankerByLadderOrderedByPoints(Ladder ladder) {
        ladder.getRankers().sort(Comparator.comparing(Ranker::getPoints).reversed());
        return ladder.getRankers();
    }

    private Ladder findLadder(Integer ladderNum) {
        return ladders.stream().filter(l -> l.getNumber().equals(ladderNum)).findFirst().orElse(null);
    }

    private Ladder findLadder(Ladder ladder) {
        return this.ladders.stream().filter(l -> l.getId().equals(ladder.getId())).findFirst().orElse(null);
    }


    public void addEvent(Integer ladderNum, EventDTO eventDTO) {
        try {
            eventSem.acquire();
            try {
                eventMap.get(ladderNum).add(eventDTO);
            } finally {
                eventSem.release();
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }

    }

    protected Ranker createNewRankerForAccountOnLadder(Account account, Integer ladderNum) {
        Ladder ladder = findLadder(ladderNum);
        if (ladder == null) ladder = createNewLadder(ladderNum);

        Ranker ranker = new Ranker(UUID.randomUUID(), ladder, account, ladder.getRankers().size() + 1);
        ladder.getRankers().add(ranker);
        return ranker;
    }


    protected Ladder createNewLadder(Integer ladderNum) {
        Ladder ladder = new Ladder(UUID.randomUUID(), ladderNum);
        ladders.add(ladder);
        return ladder;
    }


    public void updateRankerRankByLadder(Ladder ladder) {
        List<Ranker> rankerList = findAllRankerByLadderOrderedByPoints(ladder);
        for (int i = 0; i < rankerList.size(); i++) {
            rankerList.get(i).setRank(i + 1);
        }
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
            return false;
        }
        return false;
    }

    public boolean buyMulti(Long accountId, Ladder ladder) {
        try {
            Ranker ranker = findActiveRankerOfAccountOnLadder(accountId, ladder);
            BigInteger cost = UpgradeUtils.buyUpgradeCost(ranker.getLadder().getNumber(), ranker.getBias());
            if (ranker.getPower().compareTo(cost) >= 0) {
                ranker.setPoints(BigInteger.ZERO);
                ranker.setPower(BigInteger.ZERO);
                ranker.setBias(0);
                ranker.setMultiplier(ranker.getMultiplier() + 1);
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return false;
    }

    public boolean promote(Long accountId, Ladder ladder) {
        try {
            Ranker ranker = findActiveRankerOfAccountOnLadder(accountId, ladder);
            // If
            // - Ranker is #1
            // - There are enough people to promote
            // - Ranker got enough points to promote
            if (ranker.getRank() == 1 && ranker.getLadder().getRankers().size() >= FairController.PEOPLE_FOR_PROMOTE && ranker.getPoints().compareTo(FairController.POINTS_FOR_PROMOTE) >= 0) {
                ranker.setGrowing(false);
                Ranker newRanker = createNewRankerForAccountOnLadder(ranker.getAccount(), ranker.getLadder().getNumber() + 1);
                newRanker.setVinegar(ranker.getVinegar());
                newRanker.setGrapes(ranker.getGrapes());
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return false;
    }


    public boolean beAsshole(Long accountId, Ladder ladder) {
        try {
            Ranker ranker = findActiveRankerOfAccountOnLadder(accountId, ladder);
            // If
            // - Ranker is #1
            // - There are enough people to promote
            // - Ranker got enough points to promote
            // - The current Ladder is the assholeLadder or higher
            if (ranker.getRank() == 1 && ranker.getLadder().getRankers().size() >= FairController.PEOPLE_FOR_PROMOTE
                    && ranker.getPoints().compareTo(FairController.POINTS_FOR_PROMOTE) >= 0
                    && ranker.getLadder().getNumber().compareTo(FairController.ASSHOLE_LADDER) >= 0) {

                ranker.getAccount().setIsAsshole(true);
                accountRepository.save(ranker.getAccount());
                return promote(accountId, ladder);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return false;
    }


    public boolean throwVinegar(Long accountId, Ladder ladder) {
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
                    && target.getLadder().getRankers().size() >= FairController.PEOPLE_FOR_PROMOTE
                    && target.getPoints().compareTo(FairController.POINTS_FOR_PROMOTE) >= 0
                    && ranker.getVinegar().compareTo(UpgradeUtils.throwVinegarCost(target.getLadder().getNumber())) >= 0) {
                BigInteger rankerVinegar = ranker.getVinegar();
                BigInteger targetVinegar = target.getVinegar();
                if (targetVinegar.compareTo(rankerVinegar) > 0) {
                    targetVinegar = targetVinegar.subtract(rankerVinegar);
                } else {
                    targetVinegar = BigInteger.ZERO;
                    promote(target.getAccount().getId(), target.getLadder());
                }
                rankerVinegar = BigInteger.ZERO;
                target.setVinegar(targetVinegar);
                ranker.setVinegar(rankerVinegar);
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return false;
    }


    public boolean resetAllLadders() {
        try {
            List<Account> accounts = accountRepository.findAllAccountsJoinedWithRankers().stream().toList();
            long assholeCount = accounts.stream().filter(Account::getIsAsshole).count();
            if (assholeCount >= FairController.ASSHOLES_FOR_RESET) {
                rankerRepository.deleteAll();
                for (Ladder ladder : ladders) {
                    ladder.getRankers().clear();
                }

                for (Account account : accounts) {
                    account.setTimesAsshole(account.getTimesAsshole() + (account.getIsAsshole() ? 1 : 0));
                    account.setIsAsshole(false);
                    // If Account was active in the last 7 days
                    // This should prevent old and inactive accounts from getting a ranker on Restart
                    if (account.getLastLogin().plus(7, ChronoUnit.DAYS).isAfter(LocalDateTime.now())) {
                        // Create New Ranker
                        createNewRankerForAccountOnLadder(account, 1);
                    }
                    accountRepository.save(account);
                }
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return false;
    }

    private Ranker findActiveRankerOfAccountOnLadder(Long accountId, Ladder ladder) {
        return findLadder(ladder).getRankers().stream().filter(r -> r.getAccount().getId() == accountId && r.isGrowing()).findFirst().orElse(null);
    }

}
