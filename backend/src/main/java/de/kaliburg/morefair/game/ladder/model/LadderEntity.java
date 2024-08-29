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
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
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
@Accessors(chain = true)
@NoArgsConstructor
@SequenceGenerator(name = "seq_ladder", sequenceName = "seq_ladder", allocationSize = 1)
public final class LadderEntity {

  private static final Random random = new Random();
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_ladder")
  private Long id;
  @NonNull
  @Column(nullable = false)
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
  @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime createdOn = OffsetDateTime.now(ZoneOffset.UTC);
  @NonNull
  @Column(nullable = false, precision = 1000)
  private BigInteger basePointsToPromote;
  @NonNull
  @Column(nullable = false)
  private Integer scaling;

  public LadderEntity(@NonNull Integer number, @NonNull RoundEntity round,
      LadderEntity previousLadder) {
    this.number = number;
    this.roundId = round.getId();

    determineLadderType(round, previousLadder);

    if (round.getTypes().contains(RoundType.REVERSE_SCALING)) {
      // Makes Ladder 1 be Asshole Ladder Scaling etc.
      this.scaling = Math.max(round.getAssholeLadderNumber() + 1 - number, 1);
    } else {
      this.scaling = number;
    }

    // getting the pointRequirement based on the type
    BigInteger base = round.getBasePointsRequirement().multiply(BigInteger.valueOf(scaling));
    if (types.contains(LadderType.TINY)) {
      base = BigInteger.ZERO;
    } else if (types.contains(LadderType.SMALL)) {
      base = base.divide(BigInteger.valueOf(10));
    } else if (types.contains(LadderType.BIG)) {
      base = base.multiply(BigInteger.valueOf(3));
    } else if (types.contains(LadderType.GIGANTIC)) {
      base = base.multiply(BigInteger.valueOf(10));
    }
    double percentage = random.nextDouble(0.8, 1.2);

    BigDecimal baseDec = new BigDecimal(base);
    baseDec = baseDec.multiply(BigDecimal.valueOf(percentage));
    this.basePointsToPromote = baseDec.toBigInteger();
  }

  private void determineLadderType(RoundEntity round, LadderEntity previousLadder) {
    types.clear();

    Set<LadderType> previousLadderTypes = previousLadder != null
        ? previousLadder.getTypes()
        : EnumSet.noneOf(LadderType.class);
    LadderTypeBuilder builder = new LadderTypeBuilder();
    builder.setLadderNumber(number);
    builder.setRoundNumber(round.getNumber());
    builder.setAssholeLadderNumber(round.getAssholeLadderNumber());
    builder.setRoundTypes(round.getTypes());
    builder.setPreviousLadderType(previousLadderTypes);
    types = builder.build();
  }

  public int getPassingGrapes() {
    if (types.contains(LadderType.CONSOLATION)) {
      return 3;
    } else if (types.contains(LadderType.NO_HANDOUTS)) {
      return 0;
    }
    return 1;
  }

  public int getBottomGrapes() {
    if (types.contains(LadderType.BOUNTIFUL)) {
      return 3;
    } else if (types.contains(LadderType.DROUGHT)) {
      return 0;
    }
    return 1;
  }

  /**
   * This is the bonus vin multiplier, the ladder winner gets vin X (getWinningMultipler / 10).
   */
  public int getWinningMultiplier() {
    if (types.contains(LadderType.GENEROUS)) {
      return 15;
    } else if (types.contains(LadderType.STINGY)) {
      return 11;
    }
    return 12;
  }
}
