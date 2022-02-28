package de.kaliburg.morefair.ladder;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Comparator;

public class LadderUtils {
    public static boolean canPromote(Ladder ladder, Ranker ranker, Ranker firstRanker) {
        return ranker.getRank() == 1 && ranker.getId().equals(firstRanker.getId()) && isLadderUnlocked(ladder, firstRanker);
    }

    public static boolean isLadderUnlocked(@NonNull Ladder ladder) {
        return isLadderUnlocked(ladder, null);
    }

    public static boolean isLadderUnlocked(@NonNull Ladder ladder, @Nullable Ranker firstRanker) {
        if (ladder.getRankers().size() <= 0) return false;
        int rankerCount = ladder.getRankers().size();
        if (rankerCount < ladder.getRequiredRankerCountToUnlock()) return false;
        if (firstRanker == null)
            firstRanker = ladder.getRankers().stream().max(Comparator.comparing(Ranker::getPoints)).orElse(ladder.getRankers().get(0));
        return firstRanker.getPoints().compareTo(ladder.getRequiredPointsToUnlock()) >= 0;
    }
}
