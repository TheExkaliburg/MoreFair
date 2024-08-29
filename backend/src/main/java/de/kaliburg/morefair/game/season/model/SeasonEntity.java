package de.kaliburg.morefair.game.season.model;

import de.kaliburg.morefair.game.season.model.types.SeasonEndType;
import de.kaliburg.morefair.game.season.model.types.SeasonType;
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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "season", uniqueConstraints = {
    @UniqueConstraint(name = "season_uk_uuid", columnNames = "uuid"),
    @UniqueConstraint(name = "season_uk_number", columnNames = "number")
})
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
  @Column(nullable = false)
  private Integer number;
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private OffsetDateTime createdOn = OffsetDateTime.now(ZoneOffset.UTC);
  @Column
  private OffsetDateTime closedOn;
  @NonNull
  @Builder.Default
  @CollectionTable(name = "season_type", foreignKey = @ForeignKey(name = "season_type_fk_season"))
  @ElementCollection(targetClass = SeasonType.class, fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  @Fetch(FetchMode.SELECT)
  private Set<SeasonType> types = EnumSet.of(SeasonType.DEFAULT);
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private SeasonEndType endType = SeasonEndType.MANUAL;

}
