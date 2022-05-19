package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.game.ladder.LadderEntity;
import lombok.Data;

@Data
public class LadderDTO {
    private Integer number;

    public LadderDTO(LadderEntity ladder) {
        this.number = ladder.getNumber();
    }
}
