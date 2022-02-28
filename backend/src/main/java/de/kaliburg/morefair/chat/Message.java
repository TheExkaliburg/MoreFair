package de.kaliburg.morefair.chat;

import de.kaliburg.morefair.account.entity.Account;
import de.kaliburg.morefair.dto.MessageDTO;
import de.kaliburg.morefair.ladder.Ladder;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "uuid")})
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@RequiredArgsConstructor
@SequenceGenerator(name = "seq_mess", sequenceName = "seq_mess", allocationSize = 1)
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_mess")
    private Long id;
    @NonNull
    @Column(nullable = false)
    private UUID uuid;
    @NonNull
    @ManyToOne
    private Account account;
    @NonNull
    @Column(nullable = false, length = 500)
    private String message;
    @NonNull
    @ManyToOne
    @JoinColumn(name = "ladder_id", nullable = false)
    private Ladder ladder;
    @NonNull
    @Column(nullable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    public MessageDTO convertToDTO() {
        return new MessageDTO(this);
    }
}
