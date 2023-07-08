package de.kaliburg.morefair.game.chat;

import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.game.chat.dto.MessageDto;
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

  List<MessageEntity> findNewestMessagesByChatType(ChatType chatType);

  void deleteMessagesOfAccount(AccountEntity account);

  MessageDto convertToDto(MessageEntity message);
}
