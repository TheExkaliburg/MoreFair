package de.kaliburg.morefair.account.entity;

import de.kaliburg.morefair.account.type.AccountAccessRole;
import de.kaliburg.morefair.dto.AccountDetailsDTO;
import de.kaliburg.morefair.ladder.Ranker;
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
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_acc")
    private Long id;
    @NonNull
    @Column(nullable = false)
    private UUID uuid;
    @NonNull
    @Column(nullable = false)
    private String username;
    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    private List<Ranker> rankers = new ArrayList<>();
    @NonNull
    @Column(nullable = false)
    private Boolean isAsshole = false;
    @NonNull
    @Column(nullable = false)
    private Integer timesAsshole = 0;
    @Column
    private Integer lastIp;
    @NonNull
    @Column(nullable = false)
    private LocalDateTime lastLogin = LocalDateTime.now();
    @NonNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountAccessRole accessRole = AccountAccessRole.PLAYER;

    public AccountDetailsDTO convertToDTO() {
        return new AccountDetailsDTO(this);
    }

    public boolean hasModPowers(){
        return getAccessRole().equals(AccountAccessRole.MODERATOR) || getAccessRole().equals(AccountAccessRole.OWNER);
    }
}
