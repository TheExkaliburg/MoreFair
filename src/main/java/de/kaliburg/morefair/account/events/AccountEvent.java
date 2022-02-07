package de.kaliburg.morefair.account.events;

import de.kaliburg.morefair.account.entity.Account;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class AccountEvent extends ApplicationEvent {
    @Getter
    private final Account account;
    @Getter
    private final AccountEventType eventType;

    public AccountEvent(Object source, Account account, AccountEventType eventType) {
        super(source);
        this.account = account;
        this.eventType = eventType;
    }

    public enum AccountEventType {
        CREATE, UPDATE
    }
}
