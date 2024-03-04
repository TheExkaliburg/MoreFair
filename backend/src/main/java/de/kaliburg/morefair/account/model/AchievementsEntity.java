package de.kaliburg.morefair.account.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
  @NonNull
  @Column(nullable = false)
  private Boolean pressedAssholeButton = false;
}
