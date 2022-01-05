package de.kaliburg.morefair.entity;

import de.kaliburg.morefair.dto.RankerDTO;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.math.BigInteger;
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
    @Column(nullable = false, precision = 1000, scale = 0)
    private BigInteger points = BigInteger.ZERO;
    @NonNull
    @Column(nullable = false, precision = 1000, scale = 0)
    private BigInteger power = BigInteger.ONE;
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

    public Ranker addPoints(Integer points) {
        return addPoints(BigInteger.valueOf(points));
    }

    public Ranker addPoints(BigInteger points) {
        this.points = this.points.add(points);
        return this;
    }

    public Ranker addPower(BigInteger power) {
        this.power = this.power.add(power);
        return this;
    }

    public Ranker addPower(Integer power) {
        return addPower(BigInteger.valueOf(power));
    }

    public RankerDTO dto() {
        return new RankerDTO(this);
    }
}
