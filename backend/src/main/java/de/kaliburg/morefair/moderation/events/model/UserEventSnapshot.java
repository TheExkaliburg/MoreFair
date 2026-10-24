package de.kaliburg.morefair.moderation.events.model;

import de.kaliburg.morefair.statistics.records.model.LadderRecord;
import de.kaliburg.morefair.statistics.records.model.RankerRecord;
import de.kaliburg.morefair.statistics.records.model.RoundRecord;

public record UserEventSnapshot(
    RankerRecord ranker,
    LadderRecord ladder,
    RoundRecord round,
    RankerRecord target,
    RankerRecord second) {
}
