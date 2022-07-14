package de.kaliburg.morefair.account;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "account_achievements")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@RequiredArgsConstructor
public class AchievementsEntity {

  @Id
  private Long id;
  @NonNull
  @OneToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "id", nullable = false, foreignKey = @ForeignKey(name =
      "fk_achievements_account"))
  @MapsId
  private AccountEntity account;
}
