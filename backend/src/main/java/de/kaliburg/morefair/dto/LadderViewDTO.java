package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.account.entity.AccountEntity;
import de.kaliburg.morefair.game.ladder.LadderEntity;
import de.kaliburg.morefair.game.ranker.RankerEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LadderViewDTO {
    private List<RankerDTO> rankers = new ArrayList<>();
    private LadderDTO currentLadder;
    private RankerDTO firstRanker;
    private RankerPrivateDTO yourRanker;
    private Integer startRank = 0;

    public LadderViewDTO(List<RankerEntity> rankers, LadderEntity currentLadder, AccountEntity account, RankerEntity firstRanker) {
        this.firstRanker = firstRanker.convertToDto();
        startRank = rankers.get(0).getRank();
        for (RankerEntity ranker : rankers) {
            RankerDTO dto = ranker.convertToDto();
            if (ranker.getAccount().getUuid().equals(account.getUuid())) {
                yourRanker = ranker.convertToPrivateDto();
                dto = yourRanker;
                dto.setYou(true);
            }
            this.rankers.add(dto);
            if (ranker.getRank() < startRank) startRank = ranker.getRank();
        }
        this.currentLadder = currentLadder.convertToLadderDTO();
    }
}
