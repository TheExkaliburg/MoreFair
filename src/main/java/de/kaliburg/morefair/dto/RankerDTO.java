package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.entity.Ranker;
import lombok.Data;
import org.springframework.web.util.HtmlUtils;

@Data
public class RankerDTO {
    private String username;
    private Integer rank;
    private Long points;
    private Long power;
    private Integer bias;
    private Integer multiplier;
    private boolean isYou = false;

    public RankerDTO(Ranker ranker) {
        this.username = HtmlUtils.htmlEscape(ranker.getAccount().getUsername());
        this.rank = ranker.getRank();
        this.points = ranker.getPoints();
        this.power = ranker.getPower();
        this.bias = ranker.getBias();
        this.multiplier = ranker.getMultiplier();
    }
}
