package de.kaliburg.morefair.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Ladder
{
    @Id
    @GeneratedValue
    private Long id;
    @NonNull
    private UUID uuid;
    @NonNull
    private int number;
    @OneToOne(optional = true)
    private Ladder nextLadder;
    @OneToOne(optional = true)
    private Ladder pastLadder;
    @OneToMany
    private List<Ranker> rankers = new ArrayList<>();
}
