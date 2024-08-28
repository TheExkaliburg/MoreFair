package de.kaliburg.morefair.game.round.model;

import de.kaliburg.morefair.game.round.model.type.RoundType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "round", uniqueConstraints = {
    @UniqueConstraint(name = "round_uk_uuid", columnNames = "uuid"),
    @UniqueConstraint(name = "round_uk_season_number", columnNames = {"seasonId", "number"})
})
@SequenceGenerator(name = "seq_round", sequenceName = "seq_round", allocationSize = 1)
public class RoundEntity {

  private static final Random random = new Random();
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_round")
  private Long id;
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private UUID uuid = UUID.randomUUID();
  @NonNull
  @Column(nullable = false)
  private Long seasonId;
  @NonNull
  @Column(nullable = false)
  private Integer number;
  @CollectionTable(name = "round_type", foreignKey = @ForeignKey(name = "round_type_fk_round"))
  @ElementCollection(targetClass = RoundType.class, fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  @Fetch(FetchMode.SELECT)
  private Set<RoundType> types = EnumSet.of(RoundType.DEFAULT);
  @NonNull
  @Builder.Default
  @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime createdOn = OffsetDateTime.now(ZoneOffset.UTC);
  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime closedOn;
  @NonNull
  @Column(nullable = false)
  private Integer assholeLadderNumber;
  @NonNull
  @Column(nullable = false, precision = 1000)
  private BigInteger basePointsRequirement;
  @NonNull
  @Column(nullable = false)
  private Float percentageOfAdditionalAssholes;

  /**
   * Getting the Assholes needed based on the percentageOfAdditionalAssholes.
   *
   * @return the assholes needed for reset
   */
  public Integer getAssholesForReset() {
    int max = 10;
    int min = 5;

    float rnd = (max - min + 1) * getPercentageOfAdditionalAssholes() / 100;
    return min + Math.round(rnd - 0.5f);
  }

  public boolean isClosed() {
    return closedOn != null;
  }
}
