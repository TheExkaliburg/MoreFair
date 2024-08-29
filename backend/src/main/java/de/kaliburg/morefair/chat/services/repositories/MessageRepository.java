package de.kaliburg.morefair.chat.services.repositories;

import de.kaliburg.morefair.chat.model.MessageEntity;
import de.kaliburg.morefair.chat.model.types.ChatType;
import java.time.OffsetDateTime;
import java.util.List;
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

  @Query(value = "SELECT * FROM message m "
      + "WHERE m.chat_id = :chatId AND m.deleted_on IS NULL "
      + "ORDER BY m.created_on DESC", nativeQuery = true)
  List<MessageEntity> findMessagesByChatId(@Param("chatId") Long chatId, Pageable pageable);

  default List<MessageEntity> findNewestMessagesByChatId(Long chatId) {
    return findMessagesByChatId(chatId, PageRequest.of(0, MESSAGES_PER_PAGE));
  }

  @Query(value = "SELECT m.* FROM message m "
      + "JOIN chat c ON m.chat_id = c.id "
      + "WHERE c.type IN :chatTypes "
      + "ORDER BY m.created_on DESC", nativeQuery = true)
  List<MessageEntity> findMessagesByChatTypeNames(@Param("chatTypes") List<String> chatType,
      Pageable pageable);

  default List<MessageEntity> findMessagesByChatType(List<ChatType> chatType, Pageable pageable) {
    return findMessagesByChatTypeNames(chatType.stream().map(Enum::toString).toList(), pageable);
  }

  default List<MessageEntity> findNewestMessagesByChatTypes(List<ChatType> chatTypes) {
    return this.findMessagesByChatType(chatTypes, PageRequest.of(0, MESSAGES_PER_PAGE));
  }

  @Modifying
  @Query(value = "UPDATE MessageEntity m "
      + "SET m.deletedOn = :deletedOn "
      + "WHERE m.accountId = :account")
  void setDeletedOnForAccount(@Param("account") Long accountId,
      @Param("deletedOn") OffsetDateTime deletedOn);


}
