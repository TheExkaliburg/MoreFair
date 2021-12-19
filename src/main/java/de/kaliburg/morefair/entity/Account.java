package de.kaliburg.morefair.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Account
{
    @Id
    @GeneratedValue
    private Long id;
    @NonNull
    private UUID uuid;
    @NonNull
    private String username;
    @OneToMany
    private List<Ranker> rankers = new ArrayList<>();
}
