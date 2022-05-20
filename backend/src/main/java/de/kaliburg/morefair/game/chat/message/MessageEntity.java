package de.kaliburg.morefair.game.chat.message;

import de.kaliburg.morefair.account.entity.AccountEntity;
import de.kaliburg.morefair.game.chat.ChatEntity;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

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
    private UUID uuid;
    @NonNull
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_account"))
    private AccountEntity account;
    @NonNull
    @Column(nullable = false, length = 500)
    private String message;
    @Column(length = 500)
    private String metadata;
    @NonNull
    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_chat"))
    private ChatEntity chat;
    @NonNull
    @Column(nullable = false)
    private ZonedDateTime createdOn = ZonedDateTime.now();

    public MessageDTO convertToDTO() {
        return new MessageDTO(this);
    }
}
