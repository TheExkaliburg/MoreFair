package de.kaliburg.morefair.game.ranker.services.repositories;

import de.kaliburg.morefair.game.ranker.model.RankerEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RankerRepository extends JpaRepository<RankerEntity, Long> {


  @Query(value = "SELECT r from RankerEntity r where r.ladderId = :ladderId order by r.rank asc")
  List<RankerEntity> findByLadderId(long ladderId);


}
