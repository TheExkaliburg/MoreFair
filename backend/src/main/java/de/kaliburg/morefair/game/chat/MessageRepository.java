package de.kaliburg.morefair.game.chat;

import de.kaliburg.morefair.account.AccountEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

  List<MessageEntity> findTop30ByChatAndDeletedOnNullOrderByCreatedOnDesc(ChatEntity chat);

  @Query("select m from MessageEntity m where m.uuid = :uuid")
  Optional<MessageEntity> findByUuid(@Param("uuid") UUID uuid);

  @Query("select m from MessageEntity m where m.account = :account")
  List<MessageEntity> findByAccount(@Param("account") AccountEntity account);


}
