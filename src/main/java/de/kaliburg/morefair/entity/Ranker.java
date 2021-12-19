package de.kaliburg.morefair.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Ranker
{
    @Id
    @GeneratedValue
    private long id;
    @NonNull
    private UUID uuid;
    private boolean isGrowing = true;
    @NonNull
    @ManyToOne(optional = false)
    private Ladder ladder;
    @NonNull
    @ManyToOne(optional = false)
    private Account account;
    private int position;
}
