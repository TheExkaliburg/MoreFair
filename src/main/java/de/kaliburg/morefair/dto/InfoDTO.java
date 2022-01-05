package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.controller.FairController;
import lombok.Data;

import java.math.BigInteger;

@Data
public class InfoDTO {
    private Integer updateLadderStepsBeforeSync;
    private Integer updateChatStepsBeforeSync;
    private Integer ladderAreaSize;
    private BigInteger pointsForPromote;
    private Integer peopleForPromote;

    public InfoDTO() {
        updateLadderStepsBeforeSync = FairController.UPDATE_LADDER_STEPS_BEFORE_SYNC;
        updateChatStepsBeforeSync = FairController.UPDATE_CHAT_STEPS_BEFORE_SYNC;
        ladderAreaSize = FairController.LADDER_AREA_SIZE;
        pointsForPromote = FairController.POINTS_FOR_PROMOTE;
        peopleForPromote = FairController.PEOPLE_FOR_PROMOTE;
    }
}
