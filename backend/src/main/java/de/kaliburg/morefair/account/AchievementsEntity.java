package de.kaliburg.morefair.account;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

@Entity
@Table(name = "account_achievements")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@RequiredArgsConstructor
public class AchievementsEntity {

    @Id
    private Long id;
    @NonNull
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id", nullable = false, foreignKey = @ForeignKey(name =
            "fk_achievements_account"))
    @MapsId
    private AccountEntity account;
    @NonNull
    @Column(nullable = false)
    private Boolean pressedAssholeButton = false;
}
