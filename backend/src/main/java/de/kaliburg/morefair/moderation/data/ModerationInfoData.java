package de.kaliburg.morefair.moderation.data;

import de.kaliburg.morefair.account.type.AccountAccessRole;
import lombok.Data;
import lombok.NonNull;

@Data
public class ModerationInfoData {
    @NonNull
    private Integer highestLadder;
    @NonNull
    private AccountAccessRole yourAccessRole;
}
