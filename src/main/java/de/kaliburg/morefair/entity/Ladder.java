package de.kaliburg.morefair.entity;

import de.kaliburg.morefair.dto.LadderDTO;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "uuid")})
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@RequiredArgsConstructor
public class Ladder {
    @Id
    @GeneratedValue
    private Long id;
    @NonNull
    @Column(nullable = false)
    private UUID uuid;
    @NonNull
    @Column(nullable = false)
    private Integer number;
    @OneToOne
    private Ladder nextLadder;
    @OneToOne
    private Ladder pastLadder;
    @OneToMany
    private List<Ranker> rankers = new ArrayList<>();
    @NonNull
    @Column(nullable = false)
    private Integer size = 0;
    @NonNull
    @Column(nullable = false)
    private Integer growingRankerCount = 0;

    public LadderDTO dto() {
        return new LadderDTO(this);
    }
}
