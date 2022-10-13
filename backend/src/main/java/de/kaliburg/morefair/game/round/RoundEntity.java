package de.kaliburg.morefair.game.round;

import de.kaliburg.morefair.FairConfig;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.HashSet;
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

  public RoundEntity(@NonNull Integer number, FairConfig config) {
    this.number = number;
    this.baseAssholeLadder = config.getBaseAssholeLadder();

    determineRoundTypes();

    double percentage = random.nextDouble(0.5, 1.5);

    BigDecimal baseDec = new BigDecimal(config.getBasePointsToPromote());
    baseDec = baseDec.multiply(BigDecimal.valueOf(percentage));
    this.basePointsRequirement = baseDec.toBigInteger();
    this.percentageOfAdditionalAssholes = random.nextFloat(100);
  }

  private void determineRoundTypes() {
    types.clear();

    float randomFastPercentage = random.nextFloat(100);
    log.debug("Rolling randomFastPercentage for Round {}: {}%", number, randomFastPercentage);

    float randomAutoPercentage = random.nextFloat(100);
    log.debug("Rolling randomAutoPercentage for Round {}: {}%", number, randomAutoPercentage);

    if (randomFastPercentage < 20) {
      types.add(RoundType.FAST);
    }

    if (randomAutoPercentage < 10) {
      types.add(RoundType.AUTO);
    }
    else if (randomAutoPercentage > 90) {
      types.add(RoundType.CHAOS);
    }

    if (types.isEmpty()) {
      types.add(RoundType.DEFAULT);
    }

  }

  public Integer getAssholeLadderNumber() {
    int result = baseAssholeLadder + highestAssholeCount;
    result = Math.min(25, result);
    if (types.contains(RoundType.FAST)) {
      result = (result + 1) / 2;
    }
    return result;
  }

  public Integer getAssholesForReset() {
    int max = getAssholeLadderNumber();
    int min = getBaseAssholeLadder() / 2;

    return min + Math.round((max - min) * getPercentageOfAdditionalAssholes() / 100);
  }

  public Integer getModifiedBaseAssholeLadder() {
    return types.contains(RoundType.FAST) ? getBaseAssholeLadder() / 2 : getBaseAssholeLadder();
  }

  public boolean isClosed() {
    if (closedOn == null) {
      return false;
    }
    return true;
  }
}
