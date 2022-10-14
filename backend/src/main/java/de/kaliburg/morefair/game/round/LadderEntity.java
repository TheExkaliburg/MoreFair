package de.kaliburg.morefair.game.round;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Entity
@Table(name = "ladder", uniqueConstraints = {
    @UniqueConstraint(name = "uk_uuid", columnNames = "uuid"),
    @UniqueConstraint(name = "uk_number_round", columnNames = {"number", "round_id"})})
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
  @ManyToOne
  @JoinColumn(name = "round_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ladder_round"))
  private RoundEntity round;
  @OneToMany(mappedBy = "ladder", fetch = FetchType.EAGER)
  private List<RankerEntity> rankers = new ArrayList<>();
  @CollectionTable(name = "ladder_type", foreignKey = @ForeignKey(name = "fk_ladder_type_ladder"))
  @ElementCollection(targetClass = LadderType.class, fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  private Set<LadderType> types = EnumSet.noneOf(LadderType.class);
  @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime createdOn = OffsetDateTime.now(ZoneOffset.UTC);
  @NonNull
  @Column(nullable = false, precision = 1000)
  private BigInteger basePointsToPromote;

  public LadderEntity(@NonNull Integer number, @NonNull RoundEntity round) {
    this.number = number;
    this.round = round;

    determineLadderType(round);

    // getting the pointRequirement based on the type
    BigInteger base = round.getBasePointsRequirement().multiply(BigInteger.valueOf(number));
    if (types.contains(LadderType.TINY)) {
      base = BigInteger.ZERO;
    } else if (types.contains(LadderType.SMALL)) {
      base = base.divide(BigInteger.valueOf(10));
    } else if (types.contains(LadderType.BIG)) {
      base = base.multiply(BigInteger.valueOf(3));
    } else if (types.contains(LadderType.GIGANTIC)) {
      base = base.multiply(BigInteger.valueOf(10));
    }
    Random random = new Random();
    double percentage = random.nextDouble(0.8, 1.2);

    BigDecimal baseDec = new BigDecimal(base);
    baseDec = baseDec.multiply(BigDecimal.valueOf(percentage));
    this.basePointsToPromote = baseDec.toBigInteger();
  }

  private void determineLadderType(RoundEntity round) {
    Set<LadderEntity> ladders = round.getLadders();
    LadderEntity ladder =
        ladders.stream().filter(l -> l.getNumber().equals(number)).findFirst().orElse(null);

    types.clear();

    if (ladder != null) {
      log.warn("Ladder already exists, copying LadderTypes.");
      types = ladder.getTypes();
      return;
    }

    Optional<LadderEntity> ladderOptional = ladders.stream()
        .filter(l -> l.getNumber().equals(number - 1)).findFirst();
    Set<LadderType> previousLadderTypes = ladderOptional.map(LadderEntity::getTypes)
        .orElse(EnumSet.noneOf(LadderType.class));

    float randomSizePercentage = random.nextFloat(100);
    log.debug("Rolling randomSizePercentage for Ladder {} in Round {}: {}%", number,
        round.getNumber(), randomSizePercentage);
    float randomNoAutoPercentage = random.nextFloat(100);
    log.debug("Rolling randomNoAutoPercentage for Ladder {} in Round {}: {}%", number,
        round.getNumber(), randomNoAutoPercentage);

    // First ladder must be DEFAULT (or SMALL on fast rounds)
    if (number == 1) {
      if (round.getTypes().contains(RoundType.AUTO)) {
        types.add(LadderType.FREE_AUTO);
      } else if (round.getTypes().contains(RoundType.FAST)) {
        types.add(LadderType.SMALL);
      } else {
        types.add(LadderType.DEFAULT);
      }
      return;
    }

    // FAST rounds have a 1% chance to generate TINY ladders instead of SMALL
    if (round.getTypes().contains(RoundType.FAST)) {
      if (randomSizePercentage < 1) {
        types.add(LadderType.TINY);
      } else {
        types.add(LadderType.SMALL);
      }
    } else {
      /* Breakdown:
        1% TINY
        19% SMALL
        1% GIGANTIC
        19% BIG
        60% DEFAULT
        
        If previous ladder was BIG/GIGANTIC, DEFAULT chance is 80% and BIG is blocked
        */
      boolean canGenerateBig = !(previousLadderTypes.contains(LadderType.BIG)
          || previousLadderTypes.contains(LadderType.GIGANTIC));
      if (randomSizePercentage < 1) {
        types.add(LadderType.TINY);
      } else if (randomSizePercentage < 20) {
        types.add(LadderType.SMALL);
      } else if (randomSizePercentage > 99) {
        types.add(LadderType.GIGANTIC);
      } else if (randomSizePercentage > 80 && canGenerateBig) {
        types.add(LadderType.BIG);
      }
    }

    if (number >= round.getAssholeLadderNumber()) {
      types.add(LadderType.NO_AUTO);
      types.add(LadderType.ASSHOLE);
      return;
    }

    // In Auto Rounds
    if (round.getTypes().contains(RoundType.AUTO)) {
      // Higher Chance to get NO_AUTO but all other rounds are FREE_AUTO
      if (randomNoAutoPercentage < 5) {
        types.add(LadderType.NO_AUTO);
      } else if (!types.contains(LadderType.NO_AUTO)) {
        types.add(LadderType.FREE_AUTO);
      }
    } else {
      // 2% Chance to get NO_AUTO and 5% for FREE_AUTO if it isn't NO_AUTO already
      if (randomNoAutoPercentage < 2) {
        types.add(LadderType.NO_AUTO);
      } else if (randomNoAutoPercentage > 95 && !types.contains(LadderType.NO_AUTO)) {
        types.add(LadderType.FREE_AUTO);
      }
    }

    if (types.isEmpty()) {
      types.add(LadderType.DEFAULT);
    }
  }

}
