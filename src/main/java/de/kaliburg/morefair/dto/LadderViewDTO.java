package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.entity.Ladder;
import de.kaliburg.morefair.entity.Ranker;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LadderViewDTO {
    private List<RankerDTO> rankers = new ArrayList<>();
    private LadderDTO currentLadder;
    private RankerDTO firstRanker;
    private Integer startRank = 0;

    public LadderViewDTO(List<Ranker> rankers, Ladder currentLadder, Account account, Ranker firstRanker) {
        this.firstRanker = firstRanker.dto();
        startRank = rankers.get(0).getRank();
        for (Ranker ranker : rankers) {
            RankerDTO dto = ranker.dto();
            if (ranker.getAccount().getUuid() == account.getUuid()) {
                dto.setYou(true);
            }
            this.rankers.add(dto);
            if (ranker.getRank() < startRank) startRank = ranker.getRank();
        }
        this.currentLadder = currentLadder.dto();
    }
}
