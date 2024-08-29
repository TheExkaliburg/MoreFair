package de.kaliburg.morefair.game.round.services.repositories;

import de.kaliburg.morefair.game.round.model.UnlocksEntity;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnlocksRepository extends JpaRepository<UnlocksEntity, Long> {

  Optional<UnlocksEntity> findFirstByAccountIdAndRoundId(
      @NonNull Long accountId,
      @NonNull Long roundId
  );
}
