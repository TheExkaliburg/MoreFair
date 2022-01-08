package de.kaliburg.morefair.service;

import de.kaliburg.morefair.controller.FairController;
import de.kaliburg.morefair.dto.LadderViewDTO;
import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.entity.Ladder;
import de.kaliburg.morefair.entity.Ranker;
import de.kaliburg.morefair.repository.AccountRepository;
import de.kaliburg.morefair.repository.LadderRepository;
import de.kaliburg.morefair.repository.RankerRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Log4j2
public class RankerService {

    private final RankerRepository rankerRepository;
    private final LadderRepository ladderRepository;
    private final AccountRepository accountRepository;

    public RankerService(RankerRepository rankerRepository, LadderRepository ladderRepository, AccountRepository accountRepository) {
        this.rankerRepository = rankerRepository;
        this.ladderRepository = ladderRepository;
        this.accountRepository = accountRepository;
    }

    public List<Ranker> findAll() {
        return rankerRepository.findAll();
    }

    public LadderViewDTO findAllRankerByHighestLadderAreaAndAccount(Account account) {
        Ranker currentRanker = findHighestRankerByAccount(account);
        Ladder currentLadder = currentRanker.getLadder();

        assert (currentRanker.getAccount().getUuid().equals(account.getUuid()));
        assert (currentRanker.getAccount().getUsername().equals(account.getUsername()));

        List<Ranker> result = findAllRankerByLadderArea(currentRanker, currentLadder);

        LadderViewDTO ladderView = new LadderViewDTO(result, currentLadder, account, findHighestRankerByLadder(currentLadder));

        return ladderView;
    }

    public List<Ranker> findByAccount(Account account) {
        return rankerRepository.findByAccount(account);
    }

    public Ranker findHighestRankerByAccount(Account account) {
        List<Ranker> temp = rankerRepository.findHighestRankerByAccount(account);

        if (temp.size() == 0) {
            temp.add(createNewRankerForAccountOnLadder(account, 1));
        }

        assert (temp.size() == 1);

        return temp.get(0);
    }

    public Ranker findHighestRankerByLadder(Ladder ladder) {
        List<Ranker> temp = rankerRepository.findHighestRankerByLadder(ladder);

        assert (temp.size() >= 1);

        return (temp.size() == 0) ? null : temp.get(0);
    }

    public List<Ranker> findAllRankerByLadder(Ladder ladder) {
        return rankerRepository.findAllRankerByLadder(ladder);
    }

    public List<Ranker> findAllRankerByLadderOrderedByPoints(Ladder ladder) {
        return rankerRepository.findAllRankerByLadderOrderedByPoints(ladder);
    }

    public List<Ranker> findAllRankerByLadderArea(Ranker ranker, Ladder ladder) {
        List<Ranker> results = findAllRankerByLadder(ladder);
        int size = results.size();
        int rank = ranker.getRank();
        int startRank = rank + 1 - (FairController.LADDER_AREA_SIZE_SERVER / 2);
        int endRank = rank + (FairController.LADDER_AREA_SIZE_SERVER / 2) - 1;

        if (endRank > size) {
            startRank = size + 1 - FairController.LADDER_AREA_SIZE_SERVER;
            endRank = size;
        }

        if (startRank < 1) {
            startRank = 1;
            endRank = Math.min(FairController.LADDER_AREA_SIZE_SERVER, size);
        }

        final int start = startRank;
        final int end = endRank;


        // Only Remove rankers that are not rank 1 or between start and end
        results.removeIf(r -> !(r.getRank() >= start && r.getRank() <= end));
        return results;
    }

    @Transactional
    public void saveAllRankerStats(List<Ranker> rankers) {
        rankerRepository.saveAll(rankers);

        //rankers.forEach(ranker -> {
        //    rankerRepository.updateRankerStatsById(ranker.getId(), ranker.getRank(), ranker.getPoints(), ranker.getPower());
        //});
    }

    @Transactional
    public boolean buyBias(Account account) {
        Ranker ranker = findHighestRankerByAccount(account);
        long cost = Math.round(Math.pow(ranker.getLadder().getNumber() + 1, ranker.getBias()));
        if (ranker.getPoints().compareTo(BigInteger.valueOf(cost)) >= 0) {
            ranker.setPoints(BigInteger.ZERO);
            ranker.setBias(ranker.getBias() + 1);
            rankerRepository.save(ranker);
            updateRankerRankByLadder(ranker.getLadder());
            return true;
        }
        return false;
    }

    @Transactional
    public boolean buyMulti(Account account) {
        Ranker ranker = findHighestRankerByAccount(account);
        long cost = Math.round(Math.pow(ranker.getLadder().getNumber() + 1, ranker.getMultiplier()));
        if (ranker.getPower().compareTo(BigInteger.valueOf(cost)) >= 0) {
            ranker.setPoints(BigInteger.ZERO);
            ranker.setPower(BigInteger.ZERO);
            ranker.setBias(0);
            ranker.setMultiplier(ranker.getMultiplier() + 1);
            rankerRepository.save(ranker);
            updateRankerRankByLadder(ranker.getLadder());
            return true;
        }
        return false;
    }

    public void updateRankerRankByLadder(Ladder ladder) {
        List<Ranker> rankerList = findAllRankerByLadderOrderedByPoints(ladder);
        for (int i = 0; i < rankerList.size(); i++) {
            rankerList.get(i).setRank(i + 1);
        }
        saveAllRankerStats(rankerList);
    }

    @Transactional
    public boolean promote(Account account) {
        Ranker ranker = findHighestRankerByAccount(account);
        if (ranker.getRank() == 1 && ranker.getLadder().getSize() >= FairController.PEOPLE_FOR_PROMOTE && ranker.getPoints().compareTo(FairController.POINTS_FOR_PROMOTE) >= 0) {
            ranker.setGrowing(false);
            Ranker newRanker = createNewRankerForAccountOnLadder(account, ranker.getLadder().getNumber() + 1);
            newRanker.setVinegar(ranker.getVinegar());
            newRanker.setGrapes(ranker.getGrapes());
            rankerRepository.save(newRanker);
            return true;
        }
        return false;
    }

    public Ranker createNewRankerForAccountOnLadder(Account account, Integer ladderNum) {
        Ladder ladder = ladderRepository.findByNumber(ladderNum);
        if (ladder == null)
            ladder = ladderRepository.save(new Ladder(UUID.randomUUID(), ladderNum));

        Ranker ranker = new Ranker(UUID.randomUUID(), ladder, account, ladder.getSize() + 1);
        ladder.setSize(ladder.getSize() + 1);
        ladder.setGrowingRankerCount(ladder.getGrowingRankerCount() + 1);

        ladderRepository.save(ladder);
        ranker = rankerRepository.save(ranker);
        return ranker;
    }

    public boolean beAsshole(Account account) {
        Ranker ranker = findHighestRankerByAccount(account);
        if (ranker.getRank() == 1 && ranker.getLadder().getSize() >= FairController.PEOPLE_FOR_PROMOTE
                && ranker.getPoints().compareTo(FairController.POINTS_FOR_PROMOTE) >= 0
                && ranker.getLadder().getNumber().equals(FairController.ASSHOLE_LADDER)) {

            account.setIsAsshole(true);
            accountRepository.save(account);

            if (accountRepository.countAccountByIsAsshole(true) >= FairController.ASSHOLE_FOR_RESET) {
                resetAllLadders();
            } else {
                ranker.setGrowing(false);
                Ranker newRanker = createNewRankerForAccountOnLadder(account, ranker.getLadder().getNumber() + 1);
                newRanker.setVinegar(ranker.getVinegar());
                newRanker.setGrapes(ranker.getGrapes());
                rankerRepository.save(newRanker);
            }

            return true;
        }
        return false;
    }

    private void resetAllLadders() {
        // Delete all Rankers
        rankerRepository.deleteAll();

        // Go through all
        List<Account> accounts = accountRepository.findAll();
        for (Account a : accounts) {
            a.setTimesAsshole(a.getTimesAsshole() + (a.getIsAsshole() ? 1 : 0));
            a.setIsAsshole(false);
            // loginDay + 7 > today
            if (a.getLastLogin().plus(7, ChronoUnit.DAYS).isAfter(LocalDateTime.now())) {
                // TODO: delete Account or just not create Ranker
                log.info("Would delete account/ranker {} with uuid: {}", a.getUsername(), a.getUuid());
            }

            // Create New Rankers for each account
            Ladder l = ladderRepository.findByNumber(1);
            rankerRepository.save(new Ranker(UUID.randomUUID(), l, a, l.getRankers().size() + 1));
            accountRepository.save(a);
        }
    }

    public boolean throwVinegar(Account account) {
        Ranker ranker = findHighestRankerByAccount(account);
        Ranker target = findHighestRankerByLadder(ranker.getLadder());

        // if rank 1, your target is not you, you have enough people and points to promote, the target didnt already promote AND have the necessary Vinegar to throw
        if (target.getRank() == 1 && ranker.getUuid() != target.getUuid() && target.isGrowing()
                && target.getLadder().getSize() >= FairController.PEOPLE_FOR_PROMOTE
                && target.getPoints().compareTo(FairController.POINTS_FOR_PROMOTE) >= 0
                && ranker.getVinegar().compareTo(
                FairController.VINEGAR_NEEDED_TO_THROW.multiply(BigInteger.valueOf(target.getLadder().getNumber()))) >= 0) {
            BigInteger rankerVinegar = ranker.getVinegar();
            BigInteger targetVinegar = target.getVinegar();
            if (targetVinegar.compareTo(rankerVinegar) > 0) {
                targetVinegar = targetVinegar.subtract(rankerVinegar);
                rankerVinegar = BigInteger.ZERO;
            } else {
                targetVinegar = BigInteger.ZERO;
                rankerVinegar = BigInteger.ZERO;
                promote(target.getAccount());
            }
            target.setVinegar(targetVinegar);
            ranker.setVinegar(rankerVinegar);
            saveAllRankerStats(Arrays.asList(target, ranker));
            return true;
        }
        return false;
    }
}
