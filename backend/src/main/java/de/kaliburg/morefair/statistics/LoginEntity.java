package de.kaliburg.morefair.statistics;

import de.kaliburg.morefair.account.AccountEntity;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "login")
@Data
public class LoginEntity {

  private Instant createdOn = Instant.now();
  @NonNull
  private Account account;

  public LoginEntity(AccountEntity account) {
    this.account = new Account(account.getId(), account.getUsername());
  }

  @Data
  @AllArgsConstructor
  private class Account {

    private Long id;
    private String name;
  }
}


