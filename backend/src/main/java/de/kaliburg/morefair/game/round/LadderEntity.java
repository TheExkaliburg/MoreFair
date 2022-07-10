package de.kaliburg.morefair.game.round;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
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

@Entity
@Table(name = "ladder", uniqueConstraints = {
    @UniqueConstraint(name = "uq_uuid", columnNames = "uuid"),
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
  @NonNull
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private LadderType type;
  @NonNull
  @Column(nullable = false, precision = 1000)
  private BigInteger pointRequirement;

  public LadderEntity(@NonNull Integer number, @NonNull RoundEntity round) {
    this.number = number;
    this.round = round;
    this.type = determineLadderType(number, round);

    // getting the pointRequirement based on the type
    BigInteger base = round.getBasePointsRequirement().multiply(BigInteger.valueOf(number));
    if (type == LadderType.SMALL) {
      base = base.divide(BigInteger.TWO);
    } else if (type == LadderType.BIG) {
      base = base.multiply(BigInteger.TWO);
    }
    Random random = new Random();
    double percentage = random.nextDouble(0.75, 1.25);

    BigDecimal baseDec = new BigDecimal(base);
    baseDec = baseDec.multiply(BigDecimal.valueOf(percentage));
    this.pointRequirement = baseDec.toBigInteger();
  }

  private LadderType determineLadderType(Integer ladderNumber, RoundEntity round) {
    Set<LadderEntity> ladders = round.getLadders();
    LadderEntity ladder =
        ladders.stream().filter(l -> l.getNumber().equals(ladderNumber)).findFirst().orElse(null);

    if (ladder != null) {
      return ladder.getType();
    }

    LadderEntity previousLadder =
        ladders.stream().filter(l -> l.getNumber().equals(ladderNumber - 1)).findFirst()
            .orElse(null);

    if (ladderNumber == 1) {
      return LadderType.DEFAULT;
    }

    float randomPercentage = random.nextFloat(100);

    if (randomPercentage < 20) {
      return LadderType.SMALL;
    }

    if (randomPercentage > 80) {
      return LadderType.BIG;
    }

    return LadderType.DEFAULT;
  }

}
