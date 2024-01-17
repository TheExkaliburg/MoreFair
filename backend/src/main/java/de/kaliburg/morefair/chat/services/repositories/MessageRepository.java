package de.kaliburg.morefair.chat.services.repositories;

import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.chat.model.ChatType;
import de.kaliburg.morefair.chat.model.MessageEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

  int MESSAGES_PER_PAGE = 50;

  @Query("SELECT m FROM MessageEntity m WHERE m.uuid = :uuid")
  Optional<MessageEntity> findByUuid(UUID uuid);

  @Query("SELECT m FROM MessageEntity m WHERE m.chat.id = :chatId AND m.deletedOn IS NULL "
      + "ORDER BY m.createdOn DESC")
  List<MessageEntity> findMessagesByChatId(@Param("chatId") Long chatId, Pageable pageable);

  default List<MessageEntity> findNewestMessagesByChatId(Long chatId) {
    return findMessagesByChatId(chatId, PageRequest.of(0, MESSAGES_PER_PAGE));
  }

  @Query("SELECT m FROM MessageEntity m WHERE m.chat.type IN :chatTypes "
      + "ORDER BY m.createdOn DESC")
  List<MessageEntity> findMessagesByChatType(@Param("chatTypes") List<ChatType> chatType,
      Pageable pageable);

  default List<MessageEntity> findNewestMessagesByChatTypes(List<ChatType> chatTypes) {
    return findMessagesByChatType(chatTypes, PageRequest.of(0, MESSAGES_PER_PAGE));
  }

  @Modifying
  @Query("update MessageEntity m set m.deletedOn = :deletedOn where m.account = :account")
  void setDeletedOnForAccount(@Param("account") AccountEntity account,
      @Param("deletedOn") OffsetDateTime deletedOn);


}
