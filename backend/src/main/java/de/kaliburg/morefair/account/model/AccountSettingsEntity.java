package de.kaliburg.morefair.account.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "account_settings",
    uniqueConstraints = {
        @UniqueConstraint(name = "account_settings_uk_uuid", columnNames = "uuid"),
        @UniqueConstraint(name = "account_settings_uk_account", columnNames = "accountId")
    }
)
@SequenceGenerator(name = "seq_account_settings", sequenceName = "seq_account_settings", allocationSize = 1)
public class AccountSettingsEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_account_settings")
  private Long id;
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private UUID uuid = UUID.randomUUID();
  @NonNull
  @Column(nullable = false)
  private Long accountId;
  @NonNull
  @Builder.Default
  @Column(nullable = false)
  private Integer vinegarSplit = 50;

  public Integer getWineSplit() {
    return 100 - vinegarSplit;
  }
}
