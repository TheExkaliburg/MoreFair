package de.kaliburg.morefair.game.chat;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * The repository for the chat entities.
 */
@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

  @Query("select c from ChatEntity c where c.uuid = :uuid")
  Optional<ChatEntity> findByUuid(@Param("uuid") UUID uuid);

  @Query("select c from ChatEntity c where c.number = :number")
  Optional<ChatEntity> findByNumber(@Param("number") Long number);


}
