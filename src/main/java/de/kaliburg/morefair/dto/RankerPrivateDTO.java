package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.ladder.Ranker;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RankerPrivateDTO extends RankerDTO {
    private String grapes;
    private String vinegar;
    private boolean autoPromote = false;

    public RankerPrivateDTO(Ranker ranker) {
        super(ranker);
        this.grapes = ranker.getGrapes().toString();
        this.vinegar = ranker.getVinegar().toString();
        this.autoPromote = ranker.isAutoPromote();
    }
}
