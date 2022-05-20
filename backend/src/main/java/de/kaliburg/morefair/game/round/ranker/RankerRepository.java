package de.kaliburg.morefair.game.round.ranker;

import de.kaliburg.morefair.account.entity.AccountEntity;
import de.kaliburg.morefair.game.round.ladder.LadderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface RankerRepository extends JpaRepository<RankerEntity, Long> {
    @Query("SELECT r FROM RankerEntity r WHERE r.account = :account")
    List<RankerEntity> findByAccount(@Param("account") AccountEntity account);

    /*  SELECT ranker.* FROM ranker
        INNER JOIN ladder ON ranker.ladder_id = ladder.id
        WHERE ranker.account_id = ?
        GROUP BY ranker.id, ladder.number
        HAVING ladder.number = (
	        SELECT  MAX(ladder.number) FROM ranker INNER JOIN ladder
	        ON ranker.ladder_id = ladder.id
	        WHERE ranker.account_id = ?);
     */
    @Query("SELECT r FROM RankerEntity r " +
            "INNER JOIN LadderEntity l ON r.ladder = l " +
            "WHERE r.account = :account " +
            "GROUP BY r.id, l.number " +
            "HAVING l.number = (" +
            "   SELECT MAX(l.number) FROM RankerEntity r " +
            "   INNER JOIN LadderEntity l ON r.ladder = l " +
            "   WHERE r.account = :account)")
    List<RankerEntity> findHighestRankerByAccount(@Param("account") AccountEntity account);

    @Query("SELECT r FROM RankerEntity r WHERE r.ladder = :ladder")
    List<RankerEntity> findAllRankerByLadder(@Param("ladder") LadderEntity ladder);

    @Query("SELECT COUNT(r) FROM RankerEntity r WHERE r.ladder = :ladder")
    Integer countRankerByLadder(@Param("ladder") LadderEntity ladder);

    @Query("SELECT r FROM RankerEntity r WHERE r.ladder = :ladder ORDER BY r.points DESC")
    List<RankerEntity> findAllRankerByLadderOrderedByPoints(@Param("ladder") LadderEntity ladder);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RankerEntity r SET r.rank = :rank, r.points = :points, r.power = :power WHERE r.id = :id")
    void updateRankerStatsById(@Param("id") Long id, @Param("rank") Integer rank, @Param("points") BigInteger points,
            @Param("power") BigInteger power);

    @Query("SELECT r FROM RankerEntity r WHERE r.ladder = :ladder AND " +
            "r.points = (SELECT Max(r.points) FROM RankerEntity r WHERE r.ladder = :ladder)")
    List<RankerEntity> findHighestRankerByLadder(@Param("ladder") LadderEntity ladder);
}
