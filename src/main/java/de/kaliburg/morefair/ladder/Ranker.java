package de.kaliburg.morefair.ladder;

import de.kaliburg.morefair.account.entity.Account;
import de.kaliburg.morefair.dto.RankerDTO;
import de.kaliburg.morefair.dto.RankerPrivateDTO;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.math.BigDecimal;
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
    @Column(nullable = false, precision = 1000, scale = 0)
    private BigInteger grapes = BigInteger.ZERO;
    @NonNull
    @Column(nullable = false, precision = 1000, scale = 0)
    private BigInteger vinegar = BigInteger.ZERO;
    private boolean autoPromote = false;

    public Ranker addPoints(Integer points, double secondsPassed) {
        return addPoints(BigInteger.valueOf(points), secondsPassed);
    }

    public Ranker addPoints(BigInteger points, double secondsPassed) {
        BigDecimal decPoints = new BigDecimal(points);
        decPoints = decPoints.multiply(BigDecimal.valueOf(secondsPassed));
        this.points = this.points.add(decPoints.toBigInteger());
        return this;
    }

    public Ranker addPower(BigInteger power, double secondsPassed) {
        BigDecimal decPower = new BigDecimal(power);
        decPower = decPower.multiply(BigDecimal.valueOf(secondsPassed));
        this.power = this.power.add(decPower.toBigInteger());
        return this;
    }

    public Ranker addPower(Integer power, double secondsPassed) {
        return addPower(BigInteger.valueOf(power), secondsPassed);
    }

    public RankerDTO convertToDto() {
        return new RankerDTO(this);
    }

    public RankerPrivateDTO convertToPrivateDto() {
        return new RankerPrivateDTO(this);
    }

    public Ranker addVinegar(Integer vinegar, double secondsPassed) {
        return addVinegar(BigInteger.valueOf(vinegar), secondsPassed);
    }

    public Ranker addVinegar(BigInteger vinegar, double secondsPassed) {
        BigDecimal decVinegar = new BigDecimal(vinegar);
        decVinegar = decVinegar.multiply(BigDecimal.valueOf(secondsPassed));
        this.vinegar = this.vinegar.add(decVinegar.toBigInteger());
        return this;
    }

    public Ranker mulVinegar(double multiplier, double deltaSec) {
        BigDecimal decVinegar = new BigDecimal(this.vinegar);
        this.vinegar = decVinegar.multiply(BigDecimal.valueOf(Math.pow(multiplier, deltaSec))).toBigInteger();
        return this;
    }

    public Ranker addGrapes(Integer grapes, double secondsPassed) {
        return addGrapes(BigInteger.valueOf(grapes), secondsPassed);
    }

    public Ranker addGrapes(BigInteger grapes, double secondsPassed) {
        BigDecimal decGrapes = new BigDecimal(grapes);
        decGrapes = decGrapes.multiply(BigDecimal.valueOf(secondsPassed));
        this.grapes = this.grapes.add(decGrapes.toBigInteger());
        return this;
    }

}
