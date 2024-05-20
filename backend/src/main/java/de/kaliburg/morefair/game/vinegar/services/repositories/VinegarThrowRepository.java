package de.kaliburg.morefair.game.vinegar.services.repositories;

import de.kaliburg.morefair.game.vinegar.model.VinegarThrowEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VinegarThrowRepository extends JpaRepository<VinegarThrowEntity, Long> {

  Optional<VinegarThrowEntity> findBySeasonIdAndAccountIdAndTargetId(Long seasonId, Long accountId,
      Long targetId);

}
