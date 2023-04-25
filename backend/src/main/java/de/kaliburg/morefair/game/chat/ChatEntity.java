package de.kaliburg.morefair.game.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "chat", uniqueConstraints = {
    @UniqueConstraint(name = "uk_uuid", columnNames = "uuid"),
    @UniqueConstraint(name = "uk_number", columnNames = "number")})
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
  private UUID uuid = UUID.randomUUID();
  /* The game doesn't need multiple game instances, so we don't need to differentiate between
  chats from a different game
  @NonNull
  @ManyToOne
  @JoinColumn(name = "game_id", nullable = false, foreignKey = @ForeignKey(name = "fk_chat_game"))
  private GameEntity game;
   */
  @OneToMany(mappedBy = "chat", fetch = FetchType.LAZY)
  private List<MessageEntity> messages = new ArrayList<>();
  @NonNull
  @Column(nullable = false)
  private Integer number;
}
