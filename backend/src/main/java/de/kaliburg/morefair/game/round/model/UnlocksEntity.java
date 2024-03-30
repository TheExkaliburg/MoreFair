package de.kaliburg.morefair.game.round.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "unlocks", uniqueConstraints = {
    @UniqueConstraint(name = "unlocks_uk_uuid", columnNames = "uuid"),
    @UniqueConstraint(name = "unlocks_uk_account_round", columnNames = {"accountId", "roundId"})
})
@SequenceGenerator(name = "seq_unlocks", sequenceName = "seq_unlocks", allocationSize = 1)
public class UnlocksEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_unlocks")
  private Long id;
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private UUID uuid = UUID.randomUUID();
  @NonNull
  @Column(nullable = false)
  private Long accountId;
  @NonNull
  @Column(nullable = false)
  private Long roundId;
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private Boolean unlockedAutoPromote = false;
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private Boolean reachedBaseAssholeLadder = false;
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private Boolean reachedAssholeLadder = false;
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private Boolean pressedAssholeButton = false;

  // TODO: -> UnlockUtils
  public Integer calculateAssholePoints() {
    int result = 0;
    if (getReachedBaseAssholeLadder()) {
      result += 1;
    }
    if (getReachedAssholeLadder()) {
      result += 2;
    }
    if (getPressedAssholeButton()) {
      result += 7;
    }
    return result;
  }
}
