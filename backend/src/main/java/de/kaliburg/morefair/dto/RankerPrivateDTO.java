package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.game.ranker.RankerEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RankerPrivateDTO extends RankerDTO {
    private String grapes;
    private String vinegar;
    private boolean autoPromote = false;

    public RankerPrivateDTO(RankerEntity ranker) {
        super(ranker);
        this.grapes = ranker.getGrapes().toString();
        this.vinegar = ranker.getVinegar().toString();
        this.autoPromote = ranker.isAutoPromote();
    }
}
