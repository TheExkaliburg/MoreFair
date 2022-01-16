package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.persistence.entity.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class AccountDetailsDTO {
    private UUID uuid;
    private Long accountId;
    private Integer highestCurrentLadder;

    public AccountDetailsDTO(Account account) {
        this.uuid = account.getUuid();
        this.accountId = account.getId();
        this.highestCurrentLadder = Math.max(1, account.getRankers().size());
    }
}
