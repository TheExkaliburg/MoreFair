package de.kaliburg.morefair.account.events;

import de.kaliburg.morefair.account.entity.Account;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class AccountServiceEvent extends ApplicationEvent {
    @Getter
    private final Account account;
    @Getter
    private final AccountServiceEventType eventType;

    public AccountServiceEvent(Object source, Account account, AccountServiceEventType eventType) {
        super(source);
        this.account = account;
        this.eventType = eventType;
    }

    public enum AccountServiceEventType {
        CREATE, UPDATE, BAN, MUTE
    }
}
