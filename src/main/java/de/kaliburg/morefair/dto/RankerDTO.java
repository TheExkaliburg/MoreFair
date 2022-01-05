package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.entity.Ranker;
import lombok.Data;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.web.util.HtmlUtils;

import java.math.BigInteger;

@Data
public class RankerDTO {
    private Integer bias;
    private boolean isYou = false;
    private Integer multiplier;
    private BigInteger points;
    private BigInteger power;
    private Integer rank;
    private String username;
    private boolean isGrowing;

    public RankerDTO(Ranker ranker) {
        this.username = HtmlUtils.htmlEscape(StringEscapeUtils.unescapeJava(ranker.getAccount().getUsername()));
        this.rank = ranker.getRank();
        this.points = ranker.getPoints();
        this.power = ranker.getPower();
        this.bias = ranker.getBias();
        this.multiplier = ranker.getMultiplier();
        this.isGrowing = ranker.isGrowing();
    }
}
