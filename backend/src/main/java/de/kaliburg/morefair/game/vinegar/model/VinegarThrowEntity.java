package de.kaliburg.morefair.game.vinegar.model;

import de.kaliburg.morefair.events.data.VinegarData.VinegarSuccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vinegar_throw", uniqueConstraints = {
    @UniqueConstraint(name = "vinegar_throw_uk_uuid", columnNames = "uuid"),
})
@SequenceGenerator(name = "seq_vinegar_throw", sequenceName = "seq_vinegar_throw", allocationSize = 1)
public class VinegarThrowEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_vinegar_throw")
  private Long id;
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private UUID uuid = UUID.randomUUID();
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);
  @NonNull
  @Column(nullable = false)
  private Long throwerAccountId;
  @NonNull
  @Column(nullable = false)
  private Long targetAccountId;
  @NonNull
  @Column(nullable = false)
  private Long ladderId;
  @NonNull
  @Column(nullable = false)
  private BigInteger vinegarThrown;
  @NonNull
  @Column(nullable = false)
  private Integer percentageThrown;
  @NonNull
  @Column(nullable = false)
  private BigInteger vinegarDefended;
  @NonNull
  @Column(nullable = false)
  private BigInteger wineDefended;
  @NonNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private VinegarSuccessType successType;
  @NonNull
  @Column(nullable = false)
  private Integer stolenPoints;
  @NonNull
  @Column(nullable = false)
  private Integer stolenStreak;

  public boolean isSuccessful() {
    return successType == VinegarSuccessType.SUCCESS
        || successType == VinegarSuccessType.DOUBLE_SUCCESS
        || successType == VinegarSuccessType.SUCCESS_PLUS;
  }

}
