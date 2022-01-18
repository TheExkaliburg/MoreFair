package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.controller.FairController;
import lombok.Data;

import java.util.List;

@Data
public class InfoDTO {
    private String pointsForPromote;
    private Integer minimumPeopleForPromote;
    private Integer assholeLadder;
    private List<String> assholeTags;
    private String baseVinegarNeededToThrow;

    public InfoDTO() {
        pointsForPromote = FairController.POINTS_FOR_PROMOTE.toString();
        minimumPeopleForPromote = FairController.MINIMUM_PEOPLE_FOR_PROMOTE;
        assholeLadder = FairController.ASSHOLE_LADDER;
        assholeTags = FairController.ASSHOLE_TAGS;
        baseVinegarNeededToThrow = FairController.BASE_VINEGAR_NEEDED_TO_THROW.toString();
    }
}
