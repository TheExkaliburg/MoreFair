package de.kaliburg.morefair.account;

import de.kaliburg.morefair.game.round.RankerEntity;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "account", uniqueConstraints = @UniqueConstraint(name = "uk_uuid", columnNames = "uuid"))
@Getter
@Setter
@Accessors(chain = true)
// @NoArgsConstructor
@RequiredArgsConstructor
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
  private String username = "Mystery Guest";
  @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
  private List<RankerEntity> rankers = new ArrayList<>();
  @NonNull
  @Column(nullable = false)
  private Boolean isAsshole = false;
  @NonNull
  @Column(nullable = false)
  private Integer timesAsshole = 0;
  @Column
  private Integer lastIp;
  @NonNull
  @Column(nullable = false)
  private ZonedDateTime lastLogin = ZonedDateTime.now();
  @NonNull
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private AccountAccessRole accessRole = AccountAccessRole.PLAYER;

  public boolean isOwner() {
    return accessRole.equals(AccountAccessRole.OWNER);
  }

  public boolean isMod() {
    return accessRole.equals(AccountAccessRole.MODERATOR) || isOwner();
  }

  public boolean isBanned() {
    return accessRole.equals(AccountAccessRole.BANNED_PLAYER);
  }

  public boolean isMuted() {
    return accessRole.equals(AccountAccessRole.MODERATOR) || isBanned();
  }

  public List<RankerEntity> getActiveRankers() {
    return rankers.stream().filter(r -> r.isGrowing())
        .collect(Collectors.toList());
  }
}
