package de.kaliburg.morefair.chat;

import de.kaliburg.morefair.ladder.Ladder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // can't do limiting Queries ???
    //@Query("SELECT m FROM Message m WHERE m.ladder = :ladder ORDER BY m.createdOn DESC")
    List<Message> findTop30ByLadderOrderByCreatedOnDesc(Ladder ladder);
}
