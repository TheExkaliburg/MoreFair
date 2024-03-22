package de.kaliburg.morefair.game.ranker.model;

import de.kaliburg.morefair.game.unlocks.model.UnlocksEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "ranker", uniqueConstraints = {
    @UniqueConstraint(name = "uk_uuid", columnNames = "uuid"),
    @UniqueConstraint(name = "uk_account_ladder", columnNames = {"account_id", "ladder_id"})
})
@SequenceGenerator(name = "seq_ranker", sequenceName = "seq_ranker", allocationSize = 1)
public class RankerEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_ranker")
  private Long id;
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private UUID uuid = UUID.randomUUID();
  @NonNull
  @Builder.Default
  @Column(nullable = false, precision = 1000)
  private BigInteger points = BigInteger.ZERO;
  @NonNull
  @Builder.Default
  @Column(nullable = false, precision = 1000)
  private BigInteger power = BigInteger.ONE;
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private Integer bias = 0;
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private Integer multiplier = 1;
  @Builder.Default
  @Column
  private boolean growing = true;
  @NonNull
  @Column(name = "ladder_id", nullable = false)
  private Long ladderId;
  @NonNull
  @Column(name = "account_id", nullable = false)
  private Long accountId;
  @NonNull
  @Column(nullable = false)
  private Integer rank;
  @Column(nullable = false, precision = 1000, scale = 0)
  @Builder.Default
  private BigInteger grapes = BigInteger.ZERO;
  @NonNull
  @Column(nullable = false, precision = 1000, scale = 0)
  @Builder.Default
  private BigInteger vinegar = BigInteger.ZERO;
  @Column
  @Builder.Default
  private boolean autoPromote = false;
  @OneToOne(mappedBy = "ranker", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private UnlocksEntity unlocks = new UnlocksEntity(this);


  public RankerEntity addPoints(Integer points, double secondsPassed) {
    return addPoints(BigInteger.valueOf(points), secondsPassed);
  }

  public RankerEntity addPoints(BigInteger points, double secondsPassed) {
    BigDecimal decPoints = new BigDecimal(points);
    decPoints = decPoints.multiply(BigDecimal.valueOf(secondsPassed));
    this.points = this.points.add(decPoints.toBigInteger());
    return this;
  }

  public RankerEntity addPower(BigInteger power, double secondsPassed) {
    BigDecimal decPower = new BigDecimal(power);
    decPower = decPower.multiply(BigDecimal.valueOf(secondsPassed));
    this.power = this.power.add(decPower.toBigInteger());
    return this;
  }

  public RankerEntity addPower(Integer power, double secondsPassed) {
    return addPower(BigInteger.valueOf(power), secondsPassed);
  }

  public RankerEntity addVinegar(Integer vinegar, double secondsPassed) {
    return addVinegar(BigInteger.valueOf(vinegar), secondsPassed);
  }

  public RankerEntity addVinegar(BigInteger vinegar, double secondsPassed) {
    BigDecimal decVinegar = new BigDecimal(vinegar);
    decVinegar = decVinegar.multiply(BigDecimal.valueOf(secondsPassed));
    this.vinegar = this.vinegar.add(decVinegar.toBigInteger());
    return this;
  }

  public RankerEntity mulVinegar(double multiplier, double deltaSec) {
    BigDecimal decVinegar = new BigDecimal(this.vinegar);
    this.vinegar = decVinegar.multiply(BigDecimal.valueOf(Math.pow(multiplier, deltaSec)))
        .toBigInteger();
    return this;
  }

  public RankerEntity addGrapes(Integer grapes, double secondsPassed) {
    return addGrapes(BigInteger.valueOf(grapes), secondsPassed);
  }

  public RankerEntity addGrapes(BigInteger grapes, double secondsPassed) {
    BigDecimal decGrapes = new BigDecimal(grapes);
    decGrapes = decGrapes.multiply(BigDecimal.valueOf(secondsPassed));
    this.grapes = this.grapes.add(decGrapes.toBigInteger());
    return this;
  }

}
