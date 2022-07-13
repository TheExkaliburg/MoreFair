package de.kaliburg.morefair.account;

import de.kaliburg.morefair.game.round.RoundEntity;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "account_unlock", uniqueConstraints = {
    @UniqueConstraint(name = "uk_uuid", columnNames = "uuid"),
    @UniqueConstraint(name = "uk_account_round", columnNames = {"account_id", "round_id"})
})
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@RequiredArgsConstructor
@SequenceGenerator(name = "seq_unlock", sequenceName = "seq_unlock", allocationSize = 1)
public class UnlockEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_unlock")
  private Long id;
  @NonNull
  @Column(nullable = false)
  private UUID uuid = UUID.randomUUID();
  @NonNull
  @ManyToOne(optional = false)
  @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name =
      "fk_unlock_account"))
  private AccountEntity account;
  @NonNull
  @ManyToOne(optional = false)
  @JoinColumn(name = "round_id", nullable = false, foreignKey = @ForeignKey(name =
      "fk_unlock_round"))
  private RoundEntity round;
  @NonNull
  @Column(nullable = false)
  private Boolean autoPromoteUnlock = false;
}
