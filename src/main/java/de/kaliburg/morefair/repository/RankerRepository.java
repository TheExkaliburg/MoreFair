package de.kaliburg.morefair.repository;

import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.entity.Ladder;
import de.kaliburg.morefair.entity.Ranker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RankerRepository extends JpaRepository<Ranker, Long> {
    @Query("SELECT r FROM Ranker r WHERE r.account = :account")
    List<Ranker> findByAccount(@Param("account") Account account);

    /*  SELECT ranker.* FROM ranker
        INNER JOIN ladder ON ranker.ladder_id = ladder.id
        WHERE ranker.account_id = ?
        GROUP BY ranker.id, ladder.number
        HAVING ladder.number = (
	        SELECT  MAX(ladder.number) FROM ranker INNER JOIN ladder
	        ON ranker.ladder_id = ladder.id
	        WHERE ranker.account_id = ?);
     */
    @Query("SELECT r FROM Ranker r " +
            "INNER JOIN Ladder l ON r.ladder = l " +
            "WHERE r.account = :account " +
            "GROUP BY r.id, l.number " +
            "HAVING l.number = (" +
            "   SELECT MAX(l.number) FROM Ranker r " +
            "   INNER JOIN Ladder l ON r.ladder = l " +
            "   WHERE r.account = :account)")
    List<Ranker> findHighestRankerByAccount(@Param("account") Account account);

    @Query("SELECT r FROM Ranker r WHERE r.ladder = :ladder")
    List<Ranker> findAllRankerByLadder(@Param("ladder") Ladder ladder);

    @Query("SELECT COUNT(r) FROM Ranker r WHERE r.ladder = :ladder")
    Integer countRankerByLadder(@Param("ladder") Ladder ladder);

    @Query("SELECT r FROM Ranker r WHERE r.ladder = :ladder ORDER BY r.points DESC")
    List<Ranker> findAllRankerByLadderOrderedByPoints(@Param("ladder") Ladder ladder);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Ranker r SET r.rank = :rank, r.points = :points, r.power = :power WHERE r.id = :id")
    void updateRankerStatsById(@Param("id") Long id, @Param("rank") Integer rank, @Param("points") Long points, @Param("power") Long power);

    @Query("SELECT r FROM Ranker r WHERE r.ladder = :ladder AND " +
            "r.points = (SELECT Max(r.points) FROM Ranker r WHERE r.ladder = :ladder)")
    List<Ranker> findHighestPointsByLadder(@Param("ladder") Ladder ladder);
}
