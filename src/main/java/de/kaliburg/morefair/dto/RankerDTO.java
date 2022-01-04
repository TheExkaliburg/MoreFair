package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.entity.Ranker;
import lombok.Data;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.web.util.HtmlUtils;

@Data
public class RankerDTO {
    private Integer bias;
    private boolean isYou = false;
    private Integer multiplier;
    private Long points;
    private Long power;
    private Integer rank;
    private String username;

    public RankerDTO(Ranker ranker) {
        this.username = HtmlUtils.htmlEscape(StringEscapeUtils.unescapeJava(ranker.getAccount().getUsername()));
        this.rank = ranker.getRank();
        this.points = ranker.getPoints();
        this.power = ranker.getPower();
        this.bias = ranker.getBias();
        this.multiplier = ranker.getMultiplier();
    }
}
