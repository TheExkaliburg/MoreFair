package de.kaliburg.morefair.account;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class AccountServiceEvent extends ApplicationEvent {

  @Getter
  private final AccountEntity account;

  public AccountServiceEvent(Object source, AccountEntity account) {
    super(source);
    this.account = account;
  }
}
