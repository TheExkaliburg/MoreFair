package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.account.entity.AccountEntity;
import de.kaliburg.morefair.account.type.AccountAccessRole;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class AccountDetailsDTO {
    private UUID uuid;
    private Long accountId;
    private Integer highestCurrentLadder;
    private AccountAccessRole accessRole;

    public AccountDetailsDTO(AccountEntity account) {
        this.uuid = account.getUuid();
        this.accountId = account.getId();
        this.highestCurrentLadder = Math.max(1, account.getRankers().size());
        this.accessRole = account.getAccessRole();
    }
}
