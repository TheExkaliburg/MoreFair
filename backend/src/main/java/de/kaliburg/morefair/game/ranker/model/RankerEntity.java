package de.kaliburg.morefair.game.ranker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
    @UniqueConstraint(name = "ranker_uk_uuid", columnNames = "uuid"),
    @UniqueConstraint(name = "ranker_uk_account_ladder", columnNames = {"account_id", "ladder_id"})
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
  @NonNull
  @Column(nullable = false, precision = 1000, scale = 0)
  @Builder.Default
  private BigInteger wine = BigInteger.ZERO;
  @Column(nullable = false)
  @Builder.Default
  private boolean autoPromote = false;

  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private OffsetDateTime createdOn = OffsetDateTime.now(ZoneOffset.UTC);
  @Column
  private OffsetDateTime promotedOn;


  public boolean isGrowing() {
    return promotedOn == null;
  }

  public void addPoints(BigInteger points, int deltaSeconds) {
    this.points = this.points.add(points.multiply(BigInteger.valueOf(deltaSeconds)));
  }

  public void addPower(BigInteger power, int deltaSeconds) {
    this.power = this.power.add(power.multiply(BigInteger.valueOf(deltaSeconds)));
  }

  public void addPower(Integer power, int deltaSeconds) {
    addPower(BigInteger.valueOf(power), deltaSeconds);
  }

  public void addVinegar(BigInteger vinegar, int deltaSeconds) {
    this.vinegar = this.vinegar.add(vinegar.multiply(BigInteger.valueOf(deltaSeconds)));
  }

  public void decayVinegar(int deltaSeconds) {
    BigInteger decayMultiplier = BigInteger.valueOf(9975);
    BigInteger decayDivider = BigInteger.valueOf(10000);

    decayMultiplier = decayMultiplier.pow(deltaSeconds);
    decayDivider = decayDivider.pow(deltaSeconds);

    this.vinegar = this.vinegar.multiply(decayMultiplier).divide(decayDivider);
  }

  public void addWine(BigInteger wine, int deltaSeconds) {

    this.wine = this.wine.add(wine.multiply(BigInteger.valueOf(deltaSeconds)));
  }

  public void decayWine(int deltaSeconds) {
    BigInteger decayMultiplier = BigInteger.valueOf(9975);
    BigInteger decayDivider = BigInteger.valueOf(10000);

    decayMultiplier = decayMultiplier.pow(deltaSeconds);
    decayDivider = decayDivider.pow(deltaSeconds);

    this.wine = this.wine.multiply(decayMultiplier).divide(decayDivider);
  }

  public void addGrapes(BigInteger grapes, int deltaSeconds) {
    this.grapes = this.grapes.add(grapes.multiply(BigInteger.valueOf(deltaSeconds)))
        .max(BigInteger.ZERO);
  }


}
