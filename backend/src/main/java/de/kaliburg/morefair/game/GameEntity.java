package de.kaliburg.morefair.game;

import de.kaliburg.morefair.game.chat.ChatEntity;
import de.kaliburg.morefair.game.round.RoundEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * The Entity that contains everything that the game is made up of.
 */
@Entity
@Table(name = "game", uniqueConstraints = @UniqueConstraint(name = "uk_uuid", columnNames = "uuid"))
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@SequenceGenerator(name = "seq_game", sequenceName = "seq_game", allocationSize = 1)
public class GameEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_game")
  private Long id;
  @NonNull
  @Column(nullable = false)
  private UUID uuid = UUID.randomUUID();
  @OneToMany(mappedBy = "game", fetch = FetchType.LAZY)
  private List<RoundEntity> rounds = new ArrayList<>();
  @ManyToOne
  @JoinColumn(name = "current_round_id", foreignKey = @ForeignKey(name = "fk_game_round"))
  private RoundEntity currentRound;
  @OneToMany(mappedBy = "game", fetch = FetchType.LAZY)
  private List<ChatEntity> chats = new ArrayList<>();
}
