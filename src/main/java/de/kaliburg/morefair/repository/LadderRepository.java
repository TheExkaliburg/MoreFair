package de.kaliburg.morefair.repository;

import de.kaliburg.morefair.entity.Ladder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LadderRepository extends JpaRepository<Ladder, Long>
{

}
