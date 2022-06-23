package de.kaliburg.morefair.game.chat.message;

import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.game.chat.ChatEntity;
import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "message", uniqueConstraints = @UniqueConstraint(name = "uk_uuid", columnNames = "uuid"))
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@RequiredArgsConstructor
@SequenceGenerator(name = "seq_message", sequenceName = "seq_message", allocationSize = 1)
public class MessageEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_message")
  private Long id;
  @NonNull
  @Column(nullable = false)
  private UUID uuid = UUID.randomUUID();
  @NonNull
  @ManyToOne
  @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_account"))
  private AccountEntity account;
  @NonNull
  @Column(nullable = false, length = 512)
  private String message;
  @Column(length = 512)
  private String metadata;
  @NonNull
  @ManyToOne
  @JoinColumn(name = "chat_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_chat"))
  private ChatEntity chat;
  @NonNull
  @Column(nullable = false)
  private ZonedDateTime createdOn = ZonedDateTime.now();
}
