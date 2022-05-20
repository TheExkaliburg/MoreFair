package de.kaliburg.morefair.game.chat;

import de.kaliburg.morefair.game.GameEntity;
import de.kaliburg.morefair.game.chat.message.MessageEntity;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "game", uniqueConstraints = @UniqueConstraint(name = "uk_uuid", columnNames = "uuid"))
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@RequiredArgsConstructor
@SequenceGenerator(name = "seq_chat", sequenceName = "seq_chat", allocationSize = 1)
public class ChatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_chat")
    private Long id;
    @NonNull
    @Column(nullable = false)
    private UUID uuid;
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false, foreignKey = @ForeignKey(name = "fk_chat_game"))
    private GameEntity game;
    @OneToMany(mappedBy = "chat", fetch = FetchType.EAGER)
    private List<MessageEntity> messages = new ArrayList<>();
}
