package de.kaliburg.morefair.account;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class AccountServiceEvent extends ApplicationEvent {

  @Getter
  private final AccountEntity account;
  @Getter
  private final AccountServiceEventType eventType;

  public AccountServiceEvent(Object source, AccountEntity account,
      AccountServiceEventType eventType) {
    super(source);
    this.account = account;
    this.eventType = eventType;
  }

  public enum AccountServiceEventType {
    CREATE, UPDATE, BAN, MUTE
  }
}
