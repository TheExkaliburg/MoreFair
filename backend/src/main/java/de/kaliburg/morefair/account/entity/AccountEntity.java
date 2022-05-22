package de.kaliburg.morefair.account.entity;

import de.kaliburg.morefair.account.type.AccountAccessRole;
import de.kaliburg.morefair.dto.AccountDetailsDTO;
import de.kaliburg.morefair.game.round.ranker.RankerEntity;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "account", uniqueConstraints = @UniqueConstraint(name = "uk_uuid", columnNames = "uuid"))
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@RequiredArgsConstructor
@SequenceGenerator(name = "seq_account", sequenceName = "seq_account", allocationSize = 1)
public class AccountEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_account")
  private Long id;
  @NonNull
  @Column(nullable = false)
  private UUID uuid;
  @NonNull
  @Column(nullable = false)
  private String username;
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

  public AccountDetailsDTO convertToDTO() {
    return new AccountDetailsDTO(this);
  }

  public boolean hasModPowers() {
    return getAccessRole().equals(AccountAccessRole.MODERATOR) || getAccessRole().equals(
        AccountAccessRole.OWNER);
  }
}
