package de.kaliburg.morefair.game.message;

import de.kaliburg.morefair.game.ladder.LadderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    // can't do limiting Queries ???
    //@Query("SELECT m FROM Message m WHERE m.ladder = :ladder ORDER BY m.createdOn DESC")
    List<MessageEntity> findTop30ByLadderOrderByCreatedOnDesc(LadderEntity ladder);
}
