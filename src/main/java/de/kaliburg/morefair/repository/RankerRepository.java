package de.kaliburg.morefair.repository;

import de.kaliburg.morefair.entity.Account;
import de.kaliburg.morefair.entity.Ladder;
import de.kaliburg.morefair.entity.Ranker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RankerRepository extends JpaRepository<Ranker, Long>
{
    @Query("SELECT r FROM Ranker r WHERE r.account = :account")
    List<Ranker> findByAccount(@Param("account") Account account);

    // SELECT * from demo t WHERE t.id = (SELECT Max(r.id) as max_id FROM demo r WHERE r.Name = "SQL");
    @Query("SELECT r FROM Ranker r WHERE r.ladder.number = (SELECT Max(r.ladder.number) FROM Ranker r WHERE r.account = :account)")
    List<Ranker> findHighestByAccount(@Param("account") Account account);

    @Query("SELECT r FROM Ranker r WHERE r.ladder = :ladder")
    List<Ranker> findAllRankerByLadder(@Param("ladder") Ladder ladder);

    @Query("SELECT COUNT(r) FROM Ranker r WHERE r.ladder = :ladder")
    Integer countRankerByLadder(@Param("ladder") Ladder ladder);

    @Query("SELECT r FROM Ranker r WHERE r.ladder = :ladder ORDER BY r.points DESC")
    List<Ranker> findAllRankerByLadderOrderedByPoints(@Param("ladder") Ladder ladder);

    @Modifying(clearAutomatically=true)
    @Query("UPDATE Ranker r SET r.points = :points, r.power = :power WHERE r.id = :id")
    void updateRankerStatsById(@Param("id")Long id, @Param("points") Long points, @Param("power") Long power);
}
