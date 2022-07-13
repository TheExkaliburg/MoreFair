package de.kaliburg.morefair.game.round;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "ranker_unlocks")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@RequiredArgsConstructor
public class UnlocksEntity {

  @Id
  private Long id;

  @NonNull
  @OneToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "id", nullable = false, foreignKey = @ForeignKey(name =
      "fk_unlocks_ranker"))
  @MapsId
  private RankerEntity ranker;

  @NonNull
  @Column(nullable = false)
  private Boolean autoPromote = false;

  public void copy(UnlocksEntity entity) {
    this.setAutoPromote(entity.getAutoPromote());
  }
}