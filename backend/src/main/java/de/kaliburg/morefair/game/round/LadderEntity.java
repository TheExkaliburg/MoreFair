package de.kaliburg.morefair.game.round;

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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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
  @Fetch(FetchMode.SELECT)
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

    LadderTypeBuilder builder = new LadderTypeBuilder();
    builder.setLadderNumber(number);
    builder.setRoundNumber(round.getNumber());
    builder.setAssholeLadderNumber(round.getAssholeLadderNumber());
    builder.setRoundTypes(round.getTypes());
    builder.setPreviousLadderType(previousLadderTypes);
    types = builder.build();
  }
}
