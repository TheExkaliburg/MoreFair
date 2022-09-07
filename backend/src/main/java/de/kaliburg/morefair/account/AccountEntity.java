package de.kaliburg.morefair.account;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "account", uniqueConstraints = @UniqueConstraint(name = "uk_uuid", columnNames = "uuid"))
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
  // Exists technically, but we don't want to call this in this direction
  //@OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
  //private List<RankerEntity> rankers = new ArrayList<>();

  @Column
  private String username;
  @Column
  private String password;

  @NonNull
  @Column(nullable = false)
  private Integer assholePoints = 0;
  @NonNull
  @Column(nullable = false)
  private Integer legacyAssholePoints = 0;
  @Column
  private Integer lastIp;
  @NonNull
  @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime lastLogin = OffsetDateTime.now(ZoneOffset.UTC);
  @NonNull
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private AccountAccessRole accessRole = AccountAccessRole.PLAYER;
  @OneToOne(mappedBy = "account", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private AchievementsEntity achievements = new AchievementsEntity(this);

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
    return accessRole.equals(AccountAccessRole.MUTED_PLAYER) || isBanned();
  }

  /**
   * Maps the saved assholePoints to the asshole Count that determines the asshole tag. The function
   * used is the inverse gauss sum formula.
   *
   * @return the asshole count
   */
  public @NonNull Integer getAssholeCount() {
    if (assholePoints <= 0) {
      return 0;
    }

    double tenth = (double) assholePoints / 10;
    double sqrt = Math.sqrt(1 + 8 * tenth);
    double solution = (-1 + sqrt) / 2;

    return (int) Math.round(Math.floor(solution));
  }
}
