package de.kaliburg.morefair.ladder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface LadderRepository extends JpaRepository<Ladder, Long> {
    @Query("SELECT l FROM Ladder l WHERE l.number = :number")
    Ladder findByNumber(@Param("number") int number);

    @Query("SELECT l FROM Ladder l LEFT JOIN FETCH l.messages")
    Set<Ladder> findAllLaddersJoinedWithMessages();

    @Query("SELECT l FROM Ladder l LEFT JOIN FETCH l.rankers")
    Set<Ladder> findAllLaddersJoinedWithRankers();

    @Query("SELECT l FROM Ladder l LEFT JOIN FETCH l.messages WHERE l.uuid = :uuid")
    Ladder findLadderByUUIDWithMessage(@Param("uuid") UUID uuid);

    @Query("SELECT l FROM Ladder l LEFT JOIN FETCH l.rankers WHERE l.uuid = :uuid")
    Ladder findLadderByUUIDWithRanker(@Param("uuid") UUID uuid);
}
