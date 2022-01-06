package de.kaliburg.morefair.entity;

import de.kaliburg.morefair.dto.AccountDetailsDTO;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.LocalDateTime;
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
@SequenceGenerator(name = "seq_acc", sequenceName = "seq_acc", allocationSize = 1)
public class Account {
    @NonNull
    @Column
    private Boolean isMuted = false;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_acc")
    private Long id;
    @NonNull
    @Column(nullable = false)
    private UUID uuid;
    @NonNull
    @Column(nullable = false)
    private String username;
    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private List<Ranker> rankers = new ArrayList<>();
    @NonNull
    @Column(nullable = false)
    private Boolean isAsshole = false;
    @NonNull
    @Column(nullable = false)
    private Integer timesAsshole = 0;
    @NonNull
    @Column(nullable = false)
    private LocalDateTime lastLogin = LocalDateTime.now();

    public AccountDetailsDTO dto() {
        return new AccountDetailsDTO(this);
    }
}
