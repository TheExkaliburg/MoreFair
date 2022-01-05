package de.kaliburg.morefair.service;

import de.kaliburg.morefair.entity.Ladder;
import de.kaliburg.morefair.repository.LadderRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

@Service
public class LadderService {


    private final LadderRepository ladderRepository;

    public LadderService(LadderRepository ladderRepository) {
        this.ladderRepository = ladderRepository;
    }

    @PostConstruct
    public void init() {
        if (ladderRepository.findByNumber(1) == null) {
            ladderRepository.save(new Ladder(UUID.randomUUID(), 1));
        }
    }

    public List<Ladder> findAllLadders() {
        return ladderRepository.findAll();
    }

    public void save(Ladder ladder) {
        ladderRepository.save(ladder);
    }
}
