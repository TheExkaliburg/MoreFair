package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.controller.FairController;
import lombok.Data;

import java.util.List;

@Data
public class InfoDTO {
    private Integer updateLadderStepsBeforeSync;
    private Integer updateChatStepsBeforeSync;
    private Integer ladderAreaSize;
    private String pointsForPromote;
    private Integer peopleForPromote;
    private Integer assholeLadder;
    private List<String> assholeTags;

    public InfoDTO() {
        updateLadderStepsBeforeSync = FairController.UPDATE_LADDER_STEPS_BEFORE_SYNC;
        updateChatStepsBeforeSync = FairController.UPDATE_CHAT_STEPS_BEFORE_SYNC;
        ladderAreaSize = FairController.LADDER_AREA_SIZE;
        pointsForPromote = FairController.POINTS_FOR_PROMOTE.toString();
        peopleForPromote = FairController.PEOPLE_FOR_PROMOTE;
        assholeLadder = FairController.ASSHOLE_LADDER;
        assholeTags = FairController.ASSHOLE_TAGS;
    }
}
