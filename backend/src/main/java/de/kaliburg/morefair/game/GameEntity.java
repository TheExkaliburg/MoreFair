package de.kaliburg.morefair.game;

import de.kaliburg.morefair.game.round.RoundEntity;
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
@SequenceGenerator(name= "seq_game", sequenceName = "seq_game", allocationSize = 1)
public class GameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_game")
    private Long id;
    @NonNull
    @Column(nullable = false)
    private UUID uuid;
    @OneToMany(mappedBy = "game", fetch = FetchType.LAZY)
    private List<RoundEntity> rounds = new ArrayList<>();
}
