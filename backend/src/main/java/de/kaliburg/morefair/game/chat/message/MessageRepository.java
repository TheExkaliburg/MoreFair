package de.kaliburg.morefair.game.chat.message;

import de.kaliburg.morefair.game.chat.ChatEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

  @Query("select m from MessageEntity m where m.chat = :chat order by m.createdOn DESC")
  List<MessageEntity> findTop30ByChatOrderByCreatedOnDesc(@Param("chat") ChatEntity chat);


  @Query("select m from MessageEntity m where m.uuid = :uuid")
  Optional<MessageEntity> findByUuid(@Param("uuid") UUID uuid);

}
