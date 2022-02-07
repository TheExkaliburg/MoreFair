package de.kaliburg.morefair.ladder;

import de.kaliburg.morefair.chat.Message;
import de.kaliburg.morefair.dto.ChatDTO;
import de.kaliburg.morefair.dto.LadderDTO;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@RequiredArgsConstructor
@SequenceGenerator(name = "seq_ladder", sequenceName = "seq_ladder", allocationSize = 1)
public class Ladder {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_ladder")
    private Long id;
    @NonNull
    @Column(nullable = false, unique = true)
    private UUID uuid;
    @NonNull
    @Column(nullable = false, unique = true)
    private Integer number;
    @OneToMany(mappedBy = "ladder", fetch = FetchType.LAZY)
    private List<Ranker> rankers = new ArrayList<>();
    @OneToMany(mappedBy = "ladder", fetch = FetchType.LAZY)
    private List<Message> messages = new ArrayList<>();

    public LadderDTO convertToLadderDTO() {
        return new LadderDTO(this);
    }

    public ChatDTO convertToChatDTO() {
        return new ChatDTO(this);
    }
}
