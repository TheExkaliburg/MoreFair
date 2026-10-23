package de.kaliburg.morefair.game.ladder.model;

import de.kaliburg.morefair.game.round.model.RoundEntity;
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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Slf4j
@Entity
@Table(name = "ladder", uniqueConstraints = {
    @UniqueConstraint(name = "ladder_uk_uuid", columnNames = "uuid"),
    @UniqueConstraint(name = "ladder_uk_number_round", columnNames = {"number", "round_id"})
})
@Getter
@Setter
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "seq_ladder", sequenceName = "seq_ladder", allocationSize = 1)
public final class LadderEntity {

  private static final Random random = new Random();
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_ladder")
  private Long id;
  @NonNull
  @Column(nullable = false)
  @Builder.Default
  private UUID uuid = UUID.randomUUID();
  @NonNull
  @Column(nullable = false)
  private Integer number;
  @NonNull
  @Column(nullable = false, name = "round_id")
  private Long roundId;
  @CollectionTable(name = "ladder_type", foreignKey = @ForeignKey(name = "ladder_type_fk_ladder"))
  @ElementCollection(targetClass = LadderType.class, fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  @Fetch(FetchMode.SELECT)
  private Set<LadderType> types = EnumSet.noneOf(LadderType.class);
  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime firstReachedOn;
  @NonNull
  @Column(nullable = false, precision = 1000)
  private BigInteger basePointsToPromote;
  @NonNull
  @Column(nullable = false)
  private Integer scaling;

  public boolean hasType(LadderType requested) {
    return types.stream().anyMatch(type -> type.matches(requested));
  }

  public boolean isReached() {
    return Optional.ofNullable(firstReachedOn).isPresent();
  }

  public int getPassingGrapes() {
    if (types.contains(LadderType.CONSOLATION)) {
      return 5;
    } else if (types.contains(LadderType.VIRUS)) {
      return -1;
    }
    return 1;
  }

  public int getBottomGrapes() {
    if (types.contains(LadderType.BOUNTIFUL)) {
      return 5;
    } else if (types.contains(LadderType.LAVA)) {
      return 0;
    }
    return 1;
  }

  /**
   * This is the bonus vin multiplier, the ladder winner gets vin X (getWinningMultipler / 10).
   */
  public int getWinningVinMultiplier() {
    if (types.contains(LadderType.GENEROUS)) {
      return 20;
    } else if (types.contains(LadderType.TAXES)) {
      return 8;
    }
    return 12;
  }
}
