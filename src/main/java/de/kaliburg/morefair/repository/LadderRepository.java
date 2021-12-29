package de.kaliburg.morefair.repository;

import de.kaliburg.morefair.entity.Ladder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LadderRepository extends JpaRepository<Ladder, Long> {
    @Query("SELECT l FROM Ladder l WHERE l.number = :number")
    Ladder findByNumber(@Param("number") int number);
}
