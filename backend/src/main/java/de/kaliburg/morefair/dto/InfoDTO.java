package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.api.FairController;
import lombok.Data;

import java.util.List;

@Data
public class InfoDTO {
    private String pointsForPromote;
    private Integer minimumPeopleForPromote;
    private Integer assholeLadder;
    private List<String> assholeTags;
    private String baseVinegarNeededToThrow;
    private String baseGrapesNeededToAutoPromote;
    private Integer autoPromoteLadder;
    private Integer manualPromoteWaitTime;

    public InfoDTO(Integer maxTimeAssholes) {
        pointsForPromote = FairController.POINTS_FOR_PROMOTE.toString();
        minimumPeopleForPromote = FairController.MINIMUM_PEOPLE_FOR_PROMOTE;
        baseVinegarNeededToThrow = FairController.BASE_VINEGAR_NEEDED_TO_THROW.toString();
        autoPromoteLadder = FairController.AUTO_PROMOTE_LADDER;
        manualPromoteWaitTime = FairController.MANUAL_PROMOTE_WAIT_TIME;
        baseGrapesNeededToAutoPromote = FairController.BASE_GRAPES_NEEDED_TO_AUTO_PROMOTE.toString();
        assholeLadder = FairController.BASE_ASSHOLE_LADDER + maxTimeAssholes;
        assholeTags = FairController.ASSHOLE_TAGS.subList(0, Math.min(maxTimeAssholes + 2, FairController.ASSHOLE_TAGS.size()));
    }
}
