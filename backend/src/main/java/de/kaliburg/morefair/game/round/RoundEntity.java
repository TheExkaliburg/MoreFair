package de.kaliburg.morefair.game.round;

import de.kaliburg.morefair.FairConfig;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "round", uniqueConstraints = {
    @UniqueConstraint(name = "uk_uuid", columnNames = "uuid"),
    @UniqueConstraint(name = "uk_number", columnNames = "number")})
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
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
  private Integer number;
  @OneToMany(mappedBy = "round", fetch = FetchType.EAGER)
  private Set<LadderEntity> ladders = new HashSet<>();
  @NonNull
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private RoundType type;
  @Column(nullable = false)
  private ZonedDateTime createdOn = ZonedDateTime.now();
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

  public RoundEntity(@NonNull Integer number, FairConfig config) {
    this.number = number;
    this.type = determineRoundType();
    this.baseAssholeLadder = config.getBaseAssholeLadder();

    double percentage = random.nextDouble(0.5, 1.5);

    BigDecimal baseDec = new BigDecimal(config.getBasePointsToPromote());
    baseDec = baseDec.multiply(BigDecimal.valueOf(percentage));
    this.basePointsRequirement = baseDec.toBigInteger();
    this.percentageOfAdditionalAssholes = random.nextFloat(100);
  }

  private RoundType determineRoundType() {
    float randomPercentage = random.nextFloat(100);

    if (randomPercentage < 40) {
      return RoundType.FAST;
    }
    return RoundType.DEFAULT;
  }

  public Integer getAssholeLadderNumber() {
    if (type == RoundType.FAST) {
      return (baseAssholeLadder + highestAssholeCount) / 2;
    }
    return baseAssholeLadder + highestAssholeCount;
  }

  public Integer getAssholesForReset() {
    int max = getAssholeLadderNumber();
    int min = getBaseAssholeLadder() / 2;

    return min + Math.round((max - min) * getPercentageOfAdditionalAssholes());
  }
}
