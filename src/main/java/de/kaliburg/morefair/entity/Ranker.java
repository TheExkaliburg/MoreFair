package de.kaliburg.morefair.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@Data
@NoArgsConstructor
public class Ranker
{
    @Id
    @GeneratedValue
    private long id;
    private boolean isGrowing = true;
    @ManyToOne
    private Ladder ladder;
    @ManyToOne
    private Account account;
    private int position;

}
