package de.kaliburg.morefair.game.round;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "round", uniqueConstraints = {
    @UniqueConstraint(name = "uk_uuid", columnNames = "uuid"),
    @UniqueConstraint(name = "uk_number", columnNames = "number")})
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@RequiredArgsConstructor
@SequenceGenerator(name = "seq_round", sequenceName = "seq_round", allocationSize = 1)
public class RoundEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_round")
  private Long id;
  @NonNull
  @Column(nullable = false)
  private UUID uuid = UUID.randomUUID();
  @NonNull
  @Column(nullable = false)
  private Integer number;
  @OneToMany(mappedBy = "round", fetch = FetchType.LAZY)
  private Set<LadderEntity> ladders = new HashSet<>();

  /* The game doesn't need multiple game instances, so we don't need to differentiate between
  rounds from a different game

  @NonNull
  @ManyToOne
  @JoinColumn(name = "game_id", nullable = false, foreignKey = @ForeignKey(name = "fk_round_game"))
  private GameEntity game;
  */

  private ZonedDateTime createdOn = ZonedDateTime.now();
  @NonNull
  @Column(nullable = false)
  private Integer highestAssholeCount = 0;
}
