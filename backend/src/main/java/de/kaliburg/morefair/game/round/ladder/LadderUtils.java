package de.kaliburg.morefair.game.round.ladder;

import de.kaliburg.morefair.game.round.ranker.RankerEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Comparator;

public class LadderUtils {
    public static boolean canPromote(LadderEntity ladder, RankerEntity ranker, RankerEntity firstRanker) {
        return ranker.getRank() == 1 && ranker.getId().equals(firstRanker.getId()) && isLadderUnlocked(ladder,
                firstRanker);
    }

    public static boolean isLadderUnlocked(@NonNull LadderEntity ladder) {
        return isLadderUnlocked(ladder, null);
    }

    public static boolean isLadderUnlocked(@NonNull LadderEntity ladder, @Nullable RankerEntity firstRanker) {
        if (ladder.getRankers().size() <= 0)
            return false;
        int rankerCount = ladder.getRankers().size();
        if (rankerCount < ladder.getRequiredRankerCountToUnlock())
            return false;
        if (firstRanker == null)
            firstRanker = ladder.getRankers().stream().max(Comparator.comparing(RankerEntity::getPoints))
                    .orElse(ladder.getRankers().get(0));
        return firstRanker.getPoints().compareTo(ladder.getRequiredPointsToUnlock()) >= 0;
    }
}
