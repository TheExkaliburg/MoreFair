package de.kaliburg.morefair.game.ladder;

import de.kaliburg.morefair.api.FairController;
import de.kaliburg.morefair.game.message.MessageEntity;
import de.kaliburg.morefair.dto.ChatDTO;
import de.kaliburg.morefair.dto.LadderDTO;
import de.kaliburg.morefair.game.ranker.RankerEntity;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ladder", uniqueConstraints = @UniqueConstraint(name = "uk_uuid", columnNames = "uuid"))
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@RequiredArgsConstructor
@SequenceGenerator(name = "seq_ladder", sequenceName = "seq_ladder", allocationSize = 1)
public class LadderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_ladder")
    private Long id;
    @NonNull
    @Column(nullable = false)
    private UUID uuid;
    @NonNull
    @Column(nullable = false)
    private Integer number;
    @OneToMany(mappedBy = "ladder", fetch = FetchType.LAZY)
    private List<RankerEntity> rankers = new ArrayList<>();
    @OneToMany(mappedBy = "ladder", fetch = FetchType.LAZY)
    private List<MessageEntity> messages = new ArrayList<>();

    public LadderDTO convertToLadderDTO() {
        return new LadderDTO(this);
    }

    public ChatDTO convertToChatDTO() {
        return new ChatDTO(this);
    }

    public Integer getRequiredRankerCountToUnlock() {
        return Math.max(FairController.MINIMUM_PEOPLE_FOR_PROMOTE, getNumber());
    }

    public BigInteger getRequiredPointsToUnlock() {
        return FairController.POINTS_FOR_PROMOTE.multiply(BigInteger.valueOf(getNumber()));
    }
}
