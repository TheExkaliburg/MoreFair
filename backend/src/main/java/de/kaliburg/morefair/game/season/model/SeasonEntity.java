package de.kaliburg.morefair.game.season.model;

import de.kaliburg.morefair.game.season.model.types.SeasonEndType;
import de.kaliburg.morefair.game.season.model.types.SeasonType;
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
import java.time.OffsetDateTime;
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
@Table(name = "season", uniqueConstraints = @UniqueConstraint(name = "season_uk_uuid", columnNames = "uuid"))
@SequenceGenerator(name = "seq_season", sequenceName = "seq_season", allocationSize = 1)
public class SeasonEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_season")
  private long id;
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private UUID uuid = UUID.randomUUID();
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private OffsetDateTime createdOn = OffsetDateTime.now();
  @Column
  private OffsetDateTime closedOn;
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private SeasonType type = SeasonType.DEFAULT;
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private SeasonEndType endType = SeasonEndType.MANUAL;

}
