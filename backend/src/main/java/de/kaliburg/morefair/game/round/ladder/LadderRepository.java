package de.kaliburg.morefair.game.round.ladder;

import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LadderRepository extends JpaRepository<LadderEntity, Long> {

  @Query("SELECT l FROM LadderEntity l WHERE l.number = :number")
  LadderEntity findByNumber(
      @Param("number") int number);

  @Query("SELECT l FROM LadderEntity l LEFT JOIN FETCH l.messages")
  Set<LadderEntity> findAllLaddersJoinedWithMessages();

  @Query("SELECT l FROM LadderEntity l LEFT JOIN FETCH l.rankers")
  Set<LadderEntity> findAllLaddersJoinedWithRankers();

  @Query("SELECT l FROM LadderEntity l LEFT JOIN FETCH l.messages WHERE l.uuid = :uuid")
  LadderEntity findLadderByUUIDWithMessage(
      @Param("uuid") UUID uuid);

  @Query("SELECT l FROM LadderEntity l LEFT JOIN FETCH l.rankers WHERE l.uuid = :uuid")
  LadderEntity findLadderByUUIDWithRanker(
      @Param("uuid") UUID uuid);
}
