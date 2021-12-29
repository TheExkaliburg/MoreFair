package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.entity.Ladder;
import de.kaliburg.morefair.entity.Ranker;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LadderViewDTO {
    private List<RankerDTO> rankers = new ArrayList<>();
    private LadderDTO currentLadder;

    public LadderViewDTO(List<Ranker> rankers, Ladder currentLadder) {
        for (Ranker ranker : rankers) {
            this.rankers.add(ranker.dto());
        }
        this.currentLadder = currentLadder.dto();
    }
}
