package de.kaliburg.morefair.game.chat;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

  @Query("SELECT c FROM ChatEntity c WHERE c.uuid = :uuid")
  Optional<ChatEntity> findByUuid(UUID uuid);

  @Query("SELECT c FROM ChatEntity c WHERE c.type = :chatType AND c.number = :number")
  Optional<ChatEntity> findByTypeAndNumber(ChatType chatType, Integer number);
}
