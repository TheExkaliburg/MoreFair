package de.kaliburg.morefair.game.round.model;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.game.season.model.SeasonEntity;
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
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "round", uniqueConstraints = {
    @UniqueConstraint(name = "uk_uuid", columnNames = "uuid"),
    @UniqueConstraint(name = "uk_number", columnNames = "number")})
@SequenceGenerator(name = "seq_round", sequenceName = "seq_round", allocationSize = 1)
public class RoundEntity {

  private static final Random random = new Random();
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_round")
  private Long id;
  @NonNull
  @Column(nullable = false)
  private UUID uuid = UUID.randomUUID();
  @NonNull
  @Column(nullable = false)
  private Long seasonId;
  @NonNull
  @Column(nullable = false)
  private Integer number;
  @CollectionTable(name = "round_type", foreignKey = @ForeignKey(name = "fk_round_type_round"))
  @ElementCollection(targetClass = RoundType.class, fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  private Set<RoundType> types = EnumSet.noneOf(RoundType.class);
  @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime createdOn = OffsetDateTime.now(ZoneOffset.UTC);
  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime closedOn;
  @NonNull
  @Column(nullable = false)
  private Integer highestAssholeCount = 0;
  @NonNull
  @Column(nullable = false)
  private Integer baseAssholeLadder;
  @NonNull
  @Column(nullable = false, precision = 1000)
  private BigInteger basePointsRequirement;
  @NonNull
  @Column(nullable = false)
  private Float percentageOfAdditionalAssholes;

  public RoundEntity(SeasonEntity season, @NonNull Integer number, FairConfig config,
      @Nullable RoundEntity previousRound) {
    this.number = number;
    this.baseAssholeLadder = config.getBaseAssholeLadder();

    determineRoundTypes(previousRound);

    if (types.contains(RoundType.CHAOS)) {
      // CHAOS rounds add a random number of additional ladders (up to 15)
      this.highestAssholeCount = random.nextInt(16);
      if (number == 100) {
        this.highestAssholeCount = 15;
      }
    }

    double percentage = getRoundBasePointRequirementMultiplier();

    BigDecimal baseDec = new BigDecimal(config.getBasePointsToPromote());
    baseDec = baseDec.multiply(BigDecimal.valueOf(percentage));
    this.basePointsRequirement = baseDec.toBigInteger();
    this.percentageOfAdditionalAssholes = random.nextFloat(100);
  }

  public RoundEntity(SeasonEntity season, @NonNull Integer number, FairConfig config) {
    this(season, number, config, null);
  }

  private void determineRoundTypes(RoundEntity previousRound) {
    types.clear();

    RoundTypeSetBuilder builder = new RoundTypeSetBuilder();
    builder.setRoundNumber(number);
    if (previousRound != null) {
      builder.setPreviousRoundType(previousRound.getTypes());
    }

    types = builder.build();
  }

  private double getRoundBasePointRequirementMultiplier() {
    double lowerBound = 0.5f;
    double upperBound = 1.5f;

    if (types.contains(RoundType.SPECIAL_100)) {
      return upperBound;
    }

    if (types.contains(RoundType.CHAOS)) {
      lowerBound /= 2.0f;
      upperBound *= 1.25f;
    } else if (types.contains(RoundType.FAST)) {
      lowerBound /= 2.0f;
      upperBound /= 2.0f;
    } else if (types.contains(RoundType.SLOW)) {
      lowerBound *= 1.25f;
      upperBound *= 1.25f;
    }

    return random.nextDouble(lowerBound, upperBound);
  }

  public Integer getAssholeLadderNumber() {
    if (types.contains(RoundType.SPECIAL_100)) {
      return 100;
    }

    int result = baseAssholeLadder + highestAssholeCount;
    result = Math.min(25, result);
    if (types.contains(RoundType.FAST)) {
      result = (result + 1) / 2;
    } else if (types.contains(RoundType.SLOW)) {
      result += 5;
    }
    return result;
  }

  public Integer getAssholesForReset() {
    int max = getAssholeLadderNumber();
    int min = getBaseAssholeLadder() / 2;

    return min + Math.round((max - min) * getPercentageOfAdditionalAssholes() / 100);
  }

  public Integer getModifiedBaseAssholeLadder() {
    if (types.contains(RoundType.SPECIAL_100)) {
      return 50;
    }

    int result = baseAssholeLadder;
    if (types.contains(RoundType.FAST)) {
      result = getBaseAssholeLadder() / 2;
    } else if (types.contains(RoundType.SLOW)) {
      result = getBaseAssholeLadder() + 5;
    }
    return result;
  }

  public boolean isClosed() {
    if (closedOn == null) {
      return false;
    }
    return true;
  }
}
