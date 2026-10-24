package de.kaliburg.morefair.moderation.events.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "user_event")
@SequenceGenerator(name = "seq_user_event", sequenceName = "seq_user_event", allocationSize = 1)
public class UserEventEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_user_event")
  private Long id;
  @NonNull
  @Column(name = "account_id", nullable = false)
  private Long accountId;
  @NonNull
  @Builder.Default
  @Column(name = "created_on", nullable = false)
  private OffsetDateTime createdOn = OffsetDateTime.now(ZoneOffset.UTC);
  @NonNull
  @Column(name = "event_type", nullable = false)
  private String eventType;
  @Column(name = "metadata_version")
  private Integer metadataVersion;
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "metadata")
  private UserEventMetadata metadata;
  @Column(name = "snapshot_version")
  private Integer snapshotVersion;
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "snapshot")
  private UserEventSnapshot snapshot;

}
