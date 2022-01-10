package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.persistence.entity.Ladder;
import lombok.Data;

@Data
public class LadderDTO {
    private Integer number;
    private Integer size;
    private Integer growingRankerCount;

    public LadderDTO(Ladder ladder) {
        this.number = ladder.getNumber();
        this.size = ladder.getSize();
        this.growingRankerCount = ladder.getGrowingRankerCount();
    }
}
