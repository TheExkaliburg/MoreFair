package de.kaliburg.morefair.game.season.model;

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
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "achievements", uniqueConstraints = {
    @UniqueConstraint(name = "achievements_uk_uuid", columnNames = "uuid"),
    @UniqueConstraint(name = "achievements_uk_account_season",
        columnNames = {"accountId", "seasonId"}
    )
})
@SequenceGenerator(name = "seq_achievements", sequenceName = "seq_achievements", allocationSize = 1)
public class AchievementsEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_achievements")
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
  private Long seasonId;
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private Integer assholePoints = 0;
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private Integer pressedAssholeButtons = 0;

  public boolean hasPressedAssholeButton() {
    return pressedAssholeButtons > 0;
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
