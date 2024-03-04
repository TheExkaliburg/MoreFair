package de.kaliburg.morefair.account.model;

import java.util.List;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class AccountServiceEvent extends ApplicationEvent {

  @Getter
  private final List<AccountEntity> accounts;

  public AccountServiceEvent(Object source, List<AccountEntity> accounts) {
    super(source);
    this.accounts = accounts;
  }
}
