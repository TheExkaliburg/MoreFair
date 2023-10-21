package de.kaliburg.morefair.game.round;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
  @NonNull
  @Column(nullable = false)
  private Boolean reachedBaseAssholeLadder = false;
  @NonNull
  @Column(nullable = false)
  private Boolean reachedAssholeLadder = false;
  @NonNull
  @Column(nullable = false)
  private Boolean pressedAssholeButton = false;

  public void copy(UnlocksEntity entity) {
    this.setAutoPromote(entity.getAutoPromote());
    this.setReachedBaseAssholeLadder(entity.getReachedBaseAssholeLadder());
    this.setPressedAssholeButton(entity.getPressedAssholeButton());
    this.setReachedAssholeLadder(entity.getReachedAssholeLadder());
  }

  public Integer calculateAssholePoints() {
    int result = 0;
    if (getReachedBaseAssholeLadder()) {
      result += 1;
    }
    if (getReachedAssholeLadder()) {
      result += 2;
    }
    if (getPressedAssholeButton()) {
      result += 7;
    }
    return result;
  }
}
