package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.entity.Account;
import lombok.Data;

import java.util.UUID;

@Data
public class AccountDetailsDTO {
    private UUID uuid;

    public AccountDetailsDTO(Account account) {
        this.uuid = account.getUuid();
    }
}
