package de.kaliburg.morefair.game.ladder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LadderRepository extends JpaRepository<LadderEntity, Long> {

}
