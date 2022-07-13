package de.kaliburg.morefair.account;

import de.kaliburg.morefair.game.round.RankerEntity;
import de.kaliburg.morefair.game.round.RoundEntity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
  private String username = "Mystery Guest";
  @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
  private List<RankerEntity> rankers = new ArrayList<>();
  @NonNull
  @Column(nullable = false)
  private Integer assholePoints = 0;
  @Column
  private Integer lastIp;
  @NonNull
  @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime lastLogin = OffsetDateTime.now(ZoneOffset.UTC);
  @NonNull
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private AccountAccessRole accessRole = AccountAccessRole.PLAYER;
  //@OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
  //@MapKeyJoinColumn(name = "round_id")
  //private Map<RoundEntity, UnlockEntity> roundUnlockMap = new HashMap<>();
  @OneToOne(mappedBy = "account", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private AchievementEntity achievement = new AchievementEntity(this);

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
    return rankers.stream().filter(RankerEntity::isGrowing).collect(Collectors.toList());
  }

  public List<RankerEntity> getCurrentRankers(RoundEntity currentRound) {
    return rankers.stream()
        .filter(r -> r.getLadder().getRound().getUuid().equals(currentRound.getUuid()))
        .collect(Collectors.toList());
  }

  public List<RankerEntity> getCurrentActiveRankers(RoundEntity currentRound) {
    return rankers.stream()
        .filter(
            r -> r.isGrowing() && r.getLadder().getRound().getUuid().equals(currentRound.getUuid()))
        .collect(Collectors.toList());
  }

  public Integer getHighestCurrentLadder(RoundEntity currentRound) {
    return getCurrentRankers(currentRound).stream().mapToInt(r -> r.getLadder().getNumber()).max()
        .orElse(1);
  }

  public @NonNull Integer getAssholeCount() {
    return assholePoints;
  }
}
