package de.kaliburg.morefair.account.model;

import de.kaliburg.morefair.account.model.types.AccountAccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "account",
    uniqueConstraints = {
        @UniqueConstraint(name = "account_uk_uuid", columnNames = "uuid"),
        @UniqueConstraint(name = "account_uk_username", columnNames = "username")
    }
)
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@SequenceGenerator(name = "seq_account", sequenceName = "seq_account", allocationSize = 1)
public class AccountEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_account")
  private Long id;
  @NonNull
  @Column(nullable = false)
  private UUID uuid = UUID.randomUUID();
  @NonNull
  @Column(nullable = false)
  private String displayName = "Mystery Guest";
  @Column(nullable = false)
  @NonNull
  private String username;
  @NonNull
  @Column(nullable = false)
  private String password;
  @Column(nullable = false)
  private boolean guest = true;

  @NonNull
  @Column(nullable = false)
  private Integer legacyAssholePoints = 0;
  @Column
  private Integer lastIp;
  @NonNull
  @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime lastLogin = OffsetDateTime.now();
  @NonNull
  @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime createdOn = OffsetDateTime.now();
  @NonNull
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private AccountAccessType accessRole = AccountAccessType.PLAYER;

  public boolean isOwner() {
    return accessRole.equals(AccountAccessType.OWNER);
  }

  public boolean isMod() {
    return accessRole.equals(AccountAccessType.MODERATOR) || isOwner();
  }

  public boolean isBanned() {
    return accessRole.equals(AccountAccessType.BANNED_PLAYER);
  }

  public boolean isMuted() {
    return accessRole.equals(AccountAccessType.MUTED_PLAYER) || isBanned();
  }

  public boolean isBroadcaster() {
    return accessRole.equals(AccountAccessType.BROADCASTER) || isOwner();
  }
}
