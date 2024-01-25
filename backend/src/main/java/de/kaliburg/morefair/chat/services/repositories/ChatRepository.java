package de.kaliburg.morefair.chat.services.repositories;

import de.kaliburg.morefair.chat.model.ChatEntity;
import de.kaliburg.morefair.chat.model.types.ChatType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

  @Query(value = "SELECT * FROM chat c "
      + "WHERE c.type = :chatType AND c.number = :number", nativeQuery = true)
  Optional<ChatEntity> findByTypeAndNumber(ChatType chatType, Integer number);
}
