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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "name_change")
@SequenceGenerator(name = "seq_name_change", sequenceName = "seq_name_change", allocationSize = 1)
public class NameChangeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_name_change")
  private Long id;
  @NonNull
  @Column(name = "account_id", nullable = false)
  private Long accountId;
  @NonNull
  @Builder.Default
  @Column(name = "created_on", nullable = false)
  private OffsetDateTime createdOn = OffsetDateTime.now(ZoneOffset.UTC);
  @NonNull
  @Column(name = "display_name", nullable = false)
  private String displayName;

}
