package de.kaliburg.morefair.chat.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "message", uniqueConstraints = @UniqueConstraint(name = "message_uk_uuid", columnNames = "uuid"))
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@RequiredArgsConstructor
@SequenceGenerator(name = "seq_message", sequenceName = "seq_message", allocationSize = 1)
public class MessageEntity implements Comparable<MessageEntity> {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_message")
  private Long id;
  @NonNull
  @Column(nullable = false)
  private UUID uuid = UUID.randomUUID();
  @NonNull
  @Column(nullable = false)
  private Long accountId;
  @NonNull
  @Column(nullable = false, length = 512)
  private String message;
  @Column(length = 512)
  private String metadata;
  @NonNull
  @Column(name = "chat_id", nullable = false)
  private Long chatId;
  @Nullable
  @Column(name = "sent_in_ladder_id")
  private Long sentInLadderId;

  @NonNull
  @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime createdOn = OffsetDateTime.now(ZoneOffset.UTC);
  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime deletedOn;

  public boolean isDeleted() {
    return deletedOn != null;
  }

  @Override
  public int compareTo(@NotNull MessageEntity o) {
    return o.getCreatedOn().compareTo(this.getCreatedOn());
  }
}
