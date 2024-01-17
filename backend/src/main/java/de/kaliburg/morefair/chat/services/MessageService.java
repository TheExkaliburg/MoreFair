package de.kaliburg.morefair.chat.services;

import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.chat.model.ChatEntity;
import de.kaliburg.morefair.chat.model.ChatType;
import de.kaliburg.morefair.chat.model.MessageEntity;
import de.kaliburg.morefair.chat.model.dto.MessageDto;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Service that manages the {@link MessageEntity} entities.
 */
public interface MessageService {

  MessageEntity find(Long id);

  MessageEntity find(UUID uuid);

  List<MessageEntity> find(ChatEntity chat);

  MessageEntity create(AccountEntity account, ChatEntity chat, String message,
      @Nullable String metadata);

  default MessageEntity create(AccountEntity account, ChatEntity chat, String message) {
    return create(account, chat, message, "[]");
  }

  List<MessageEntity> findNewestMessagesByChatType(List<ChatType> chatTypes);

  default List<MessageEntity> findNewestMessagesByChatType(ChatType chatType) {
    return findNewestMessagesByChatType(List.of(chatType));
  }

  void deleteMessagesOfAccount(AccountEntity account);

  MessageDto convertToDto(MessageEntity message);
}
