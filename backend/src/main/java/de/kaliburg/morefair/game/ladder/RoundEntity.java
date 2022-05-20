package de.kaliburg.morefair.game.ladder;

import de.kaliburg.morefair.game.GameEntity;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "round", uniqueConstraints = { @UniqueConstraint(name = "uk_uuid", columnNames = "uuid"),
        @UniqueConstraint(name = "uk_number", columnNames = "number") })
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
    @Column(nullable = false)
    private Integer number;
    @NonNull
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false, foreignKey = @ForeignKey(name = "fk_round_game"))
    private GameEntity game;
    @OneToMany(mappedBy = "round", fetch = FetchType.LAZY)
    private List<LadderEntity> ladders = new ArrayList<>();
    private ZonedDateTime createdOn = ZonedDateTime.now();

    public void test() {

    }
}
