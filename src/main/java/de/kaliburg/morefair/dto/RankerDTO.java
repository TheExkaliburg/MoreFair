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
    private String points;
    private String power;
    private Integer rank;
    private String username;
    private boolean isAsshole;
    private boolean isGrowing;

    public RankerDTO(Ranker ranker) {
        this.username = HtmlUtils.htmlEscape(StringEscapeUtils.unescapeJava(ranker.getAccount().getUsername()));
        this.rank = ranker.getRank();
        this.points = ranker.getPoints().toString();
        this.power = ranker.getPower().toString();
        this.bias = ranker.getBias();
        this.multiplier = ranker.getMultiplier();
        this.isGrowing = ranker.isGrowing();
        this.isAsshole = ranker.getAccount().getIsAsshole() || ranker.getAccount().getWasAsshole();
    }
}
