package de.kaliburg.morefair.game.round;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
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
  @ElementCollection(targetClass = LadderType.class, fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  private Set<LadderType> types = new HashSet<>();
  @NonNull
  @Column(nullable = false, precision = 1000)
  private BigInteger basePointsToPromote;

  public LadderEntity(@NonNull Integer number, @NonNull RoundEntity round) {
    this.number = number;
    this.round = round;

    determineLadderType(round);

    // getting the pointRequirement based on the type
    BigInteger base = round.getBasePointsRequirement().multiply(BigInteger.valueOf(number));
    if (types.contains(LadderType.SMALL)) {
      base = base.divide(BigInteger.valueOf(10));
    } else if (types.contains(LadderType.BIG)) {
      base = base.multiply(BigInteger.valueOf(3));
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
    }

    LadderEntity previousLadder =
        ladders.stream().filter(l -> l.getNumber().equals(number - 1)).findFirst()
            .orElse(null);

    float randomSizePercentage = random.nextFloat(100);
    log.info("Rolling randomSizePercentage for Ladder {} in Round {}: {}%", number,
        round.getNumber(), randomSizePercentage);

    if (round.getTypes().contains(RoundType.FAST)) {
      types.add(LadderType.SMALL);
    } else if (number == 1) {
      types.add(LadderType.DEFAULT);
    } else if (randomSizePercentage < 20) {
      types.add(LadderType.SMALL);
    } else if (randomSizePercentage > 80) {
      types.add(LadderType.BIG);
    }

    if (types.isEmpty()) {
      types.add(LadderType.DEFAULT);
    }
  }

}
