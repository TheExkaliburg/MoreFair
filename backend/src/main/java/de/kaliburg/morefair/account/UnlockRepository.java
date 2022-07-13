package de.kaliburg.morefair.account;

import de.kaliburg.morefair.game.round.RoundEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UnlockRepository extends JpaRepository<UnlockEntity, Long> {

  @Query("select u from UnlockEntity u where u.account = :account and u.round = :round")
  Optional<UnlockEntity> findByAccountAndRound(
      @Param("account") AccountEntity account, @Param("round") RoundEntity round);

}
