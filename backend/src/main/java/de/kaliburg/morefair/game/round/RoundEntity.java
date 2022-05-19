package de.kaliburg.morefair.game.round;

import de.kaliburg.morefair.game.GameEntity;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "round", uniqueConstraints = @UniqueConstraint(name = "uk_uuid", columnNames = "uuid"))
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@RequiredArgsConstructor
@SequenceGenerator(name = "seq_round", sequenceName = "seq_round", allocationSize = 1)
public class RoundEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_round")
    private Long id;
    @NonNull
    @Column(nullable = false)
    private UUID uuid;
    @NonNull
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false, foreignKey = @ForeignKey(name = "tb_round_fk_game"))
    private GameEntity game;
}
