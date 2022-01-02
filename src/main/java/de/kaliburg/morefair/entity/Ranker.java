package de.kaliburg.morefair.entity;

import de.kaliburg.morefair.dto.RankerDTO;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "uuid")})
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@RequiredArgsConstructor
@SequenceGenerator(name = "seq_rank", sequenceName = "seq_rank", allocationSize = 1)
public class Ranker {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_rank")
    private Long id;
    @NonNull
    @Column(nullable = false)
    private UUID uuid;
    @NonNull
    @Column(nullable = false)
    private Long points = 0L;
    @NonNull
    @Column(nullable = false)
    private Long power = 1L;
    @NonNull
    @Column(nullable = false)
    private Integer bias = 0;
    @NonNull
    @Column(nullable = false)
    private Integer multiplier = 1;
    private boolean isGrowing = true;
    @NonNull
    @ManyToOne
    @JoinColumn(name = "ladder_id", nullable = false)
    private Ladder ladder;
    @NonNull
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    @NonNull
    @Column(nullable = false)
    private Integer rank;

    public Ranker addPoints(Long points) {
        this.points += points;
        return this;
    }

    public Ranker addPoints(Integer points) {
        this.points += points;
        return this;
    }

    public Ranker addPower(Long power) {
        this.power += power;
        return this;
    }

    public Ranker addPower(Integer power) {
        this.power += power;
        return this;
    }

    public RankerDTO dto() {
        return new RankerDTO(this);
    }
}
